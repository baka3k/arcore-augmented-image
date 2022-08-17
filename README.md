
# ARCore: Augmented Images API & Sceneform

Khác với nhiều tiện ích khác, do giới hạn về phân cứng nên ARCore chỉ support trên 1 số dòng device nhất định với các API nhất định, danh sách các loại devices supported các bạn tham khảo tại link bên dưới, và chuẩn bị cho mình 1 chiếc xịn sò trước khi bắt tay vào thử nghiệm nhé:
https://developers.google.com/ar/devices

# Augmented Images API

Đây là API hỗ trợ việc detect và hỗ trợ hiện thị tăng cường cho hình ảnh 2D. Ví dụ bạn đưa camera vào 1 poster phim thì ngay lập tức chúng ta sẽ thấy poster phim đó biến thành trailer phim, hoặc khi đưa camera vào ảnh của Karen Yuzuriha, ngay lập tức chúng ta sẽ thấy Idol sống động, 'nhảy nhót' trên camera luôn chả hạn - thật tuyệt phải ko

Việc này được thực hiện tương đối dễ dàng với ARCore
 
## Chuẩn bị input

Để ARCore detect được vật thể 2D thì ARCore cần dược cung cấp input để nhận dạng vật thể. Input này sẽ là các ảnh mà người dùng muốn ARCore nhận diện(ví dụ poster phim, ảnh diễn viên...etc) hoăc database ảnh dạng vector
Note: Ảnh có thể được add thêm tại runtime, khi ứng dụng đang hoạt động.

Google cung cấp một tool có tên là **[arcoreimg](https://developers.google.com/ar/develop/augmented-images/arcoreimg)**,  gồm 2 chức năng chính

 1. Kiểm tra ảnh đưa vào nhận dạng:

```
arcoreimg eval-img --input_image_path=dog.jpg
```
Khuyến cáo của Google là bạn chỉ lên chọn những tấm ảnh với điểm số **trên** 75, dưới 75 thường rất khó để ARCore detect được

 2. Build Database input
 ```
 arcoreimg build-db --input_images_directory=/path/to/images \
                       --output_db_path=/path/to/myimages.imgdb
 ```
Dung lượng database thường được extract ra dạng vector và nhỏ hơn nếu sử dụng ảnh nguyên bản rất nhiều, nếu được thì bạn nên sử dụng DB,  tốc độ load lúc khởi tạo ban đầu tương đối nhanh hơn dùng ảnh nguyên bản
  

## ArFragment

Mình sẽ bỏ qua các config như check runtime permission cho Camera, check xem device có support ARCore hay ko - nhé. Sau khi hoàn thành tất cả, thủ tục đơn giản chỉ là add ArFragment vào Activity
```
arFragment = ArFragment()  
supportFragmentManager.beginTransaction().replace(R.id.ar_fragment, arFragment).commit()
```
  và bạn đã có một ứng dụng AR. ArFragment nằm trong gói 
  ```
  com.google.ar.sceneform:core
  com.google.ar.sceneform.ux:sceneform-ux
  ```
Khi bạn muốn dùng ARCore, add vật thể..etc mà ko muốn học thêm các kiến thức như OpenGL thì tốt nhất dùng đến 2 gói lib này. Bạn có thể handle camera, add native view lên ARScene mà ko cần đến các đoạn mã OpenGL dài ngoằng
Trong ứng dụng sample, mình muốn thay đổi một số config nên mình đã tạo ra 1 class extend từ ArFragment
```
open class AugmentedImageSampleFragment : ArFragment()
```
Vì là xử lý với Image tĩnh nên mình lựa chọn disable một số view
```
private fun configARView() {  
    planeDiscoveryController.hide()  
    planeDiscoveryController.setInstructionView(null)  
    arSceneView.planeRenderer.isEnabled = false
}
```
Và sau đó config lại AR target bằng cách override hàm getSessionConfiguration() của ArFragment
```
override fun getSessionConfiguration(session: Session?): Config {  
    val config = super.getSessionConfiguration(session)  
    ARConfig.configArTarget(session, config, requireContext())  
    return config  
}
```
## Config ARTarget
Chúng ta cần set lại Camera thành auto focus cho dễ nhận diện vật thể
```
private fun optimizeCamera(config: Config) {  
  config.focusMode = Config.FocusMode.AUTO // auto focus  
  config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE  
  config.depthMode = Config.DepthMode.AUTOMATIC  
}
```
  Sau đó config augmentedImageDatabase cho ArCore
  ```
  fun configArTarget(  session: Session?,  config: Config, context: Context  ) {  
    optimizeCamera(config) // optimize camera  
	if (USE_IMAGE_TARGET) {  
        setImageAugmentTarget(config, session, context)  
    } else {  
        setDataBaseAugmentTarget(config, session, context)  
    }  
}
  ```
Ở đây ArCore cung cấp 2 cách để config target
### Sử dụng bitmap
```
private fun setImageAugmentTarget(config: Config, session: Session?, context: Context) {  
    val augmentedImageDatabase = AugmentedImageDatabase(session)  
    IMAGES.forEach {  
  val augmentedImageBitmap: Bitmap =  
            context.assets.open(it).use { inputStream ->  
  BitmapFactory.decodeStream(  
                    inputStream  
                )  
            }  
  // add photo to detector  
 // we can add many photo at runtime  augmentedImageDatabase.addImage(it, augmentedImageBitmap)  
    }  
  config.augmentedImageDatabase = augmentedImageDatabase  
}
```
Với cách làm này, photo có thể add thêm trong quá trình ứng dụng chạy, với những ảnh có kích thước lớn sẽ hơi tốn thời gian để xử lý
### Sử dụng DataBase được build sẵn bằng arcoretool

```
private fun setDataBaseAugmentTarget(  
    config: Config,  
  session: Session?, context: Context  
) {  
    config.augmentedImageDatabase = AugmentedImageDatabase.deserialize(  
        session,  
  context.resources.assets.open(IMAGE_DATABASE)  
    )  
}
```
Ảnh sẽ được extract thành dạng vector và đưa vào database bằng arcoretool, lợi ích của cách làm này là dung lượng database tương đối nhẹ và việc load lên sẽ nhanh hơn

## Hiển thị ArScene
Đầu tiên chúng ta cần detect được vật thể mong muốn
```
arSceneView.scene.addOnUpdateListener(::onUpdateFrame)
```
Tương ứng với mỗi frame thay đổi
```
private fun onUpdateFrame(frameTime: FrameTime?) {  
    val frame = arSceneView.arFrame  
  if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {  
        //Logger.w("#onUpdateFrame() Frame is not stable - do not handle $frame")  
  return  
  }  
    frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->  
  when (image.trackingState) {  
            TrackingState.TRACKING -> {  
                if (trackableMap.contains(image.name)) {  
                    if (trackableMap[image.name]?.update(image) == true) {  
                        Logger.d("update node: ${image.name}(${image.index}), pose: ${image.centerPose}, ex: ${image.extentX}, ez: ${image.extentZ}")  
                    }  
                } else {  
                    createArNode(image)   /// add ARnode
                }  
            }  
            TrackingState.STOPPED -> {  
                Logger.d("remove node: ${image.name}(${image.index})")  
                Toast.makeText(context, "${image.name} removed", Toast.LENGTH_LONG).show()  
                trackableMap.remove(image.name).let {  
  arSceneView.scene.removeChild(it)  
                }  
  }  
            else -> {  
                Logger.w("#onUpdateFrame() Unknow State - Do nothing ${image.trackingState}")  
            }  
        }  
    }  
}
```
 Cuối cùng việc đơn giản còn lại là ad node lên arview
 ```
 private fun createArNode(image: AugmentedImage) {  
	 val node = SampleScene().init(image)  
     currentNodeName = image.name  
	 trackableMap[image.name] = node  
     arSceneView.scene.addChild(node)  
}
 ```

## ArResource
```
object ArResources {  
    fun init(context: Context): CompletableFuture<Void> {  
        videoPlayer = MediaPlayer.create(context, R.raw.sample)  
        val texture = ExternalTexture()  
        videoPlayer.setSurface(texture.surface)  
        videoPlayer.isLooping = true  
  
  videoRenderable = ModelRenderable.builder().setSource(context, com.google.ar.sceneform.rendering.R.raw.sceneform_view_renderable).build().also {  
 it.thenAccept { renderable ->  
  renderable.material.setExternalTexture("viewTexture", texture)  
            }  
 }  bayViewRenderable = ViewRenderable.builder().setView(context, R.layout.layout_bay)  
            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER).build()  
        return CompletableFuture.allOf(  
            bayViewRenderable,  
  videoRenderable  
  )  
    }  
  
    lateinit var bayViewRenderable: CompletableFuture<ViewRenderable>  
    val viewRenderableRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)  
  
    lateinit var videoRenderable: CompletableFuture<ModelRenderable>  
    lateinit var videoPlayer: MediaPlayer  
}
```
## Demo video 
[![Watch the video]](https://github.com/baka3k/arcore-augmented-image/blob/main/demo/earthdemo.mp4)

[![Watch the video]](https://github.com/baka3k/arcore-augmented-image/blob/main/demo/karendemo.mp4)

