# OkHttp
## OkHttp请求封装
### 使用方法

#### 方法一(老版,已停止维护)
module 下添加

     compile 'com.github.BenYanYi:OkHttp:1.1.6'

<br/>
project 下添加

    allprojects {
        repositories {
            jcenter()
            maven {
                url 'https://jitpack.io'
            }
        }
    }

<br/>

#### 方法二(最新)
module 下添加

    compile 'com.yanyi.benyanyi:okhttplib:1.1.1'
    
### 更新记录
* 2018/11/20(1.1.1) 添加带弹窗下载方法(UpdateUtil类),支持下载弹窗提示，下载进度弹窗，下载进度通知栏，强行下载,多文件下载类(DownloadManager)(支持取消下载)
* 2018/10/10(1.1.0) 添加写入超时限定
* 2018/09/28(1.0.9) 添加自定义Request方法
* 2018/09/14(1.0.8) 优化下载文件方法
* 2018/09/12(1.0.7) 修复下载文件回调没返回问题,下载路径都是根目录下

<br/>
若在使用过程中出现什么问题，可以联系作者<br/>
作者：演绎<br/>
QQ：1541612424<br/>
email： work@yanyi.red<br/>
微信公众号：benyanyi(演绎未来)&nbsp;&nbsp;&nbsp;将会不定期的更新关于android的一些文章