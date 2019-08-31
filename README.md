![界面1](https://github.com/leishui/Music/blob/master/app/src/main/img/1.gif)
![界面2](https://github.com/leishui/Music/blob/master/app/src/main/img/2.gif)
![界面3](https://github.com/leishui/Music/blob/master/app/src/main/img/3.gif)

**使用了：**

使用了retrofit进行网络请求
使用Shared Preference进行用户信息、登录状态、播放列表等的存取
使用通知启动前台音乐播放服务
使用pop up window进行当前播放列表的展示
通知使用广播进行音乐播放控制
监听audio focus的变化并作相应处理
监听电话状态变化并作相应处理
监听耳机插拔并作相应处理

**实现了：**

在联网的前提下手机号+密码的登录、获取用户头像昵称并展示、获取用户歌单并展示
用户信息、歌单信息等在第一次加载成功后优先从本地加载
下次打开应用时记录上次播放的歌曲和歌单
拖动seek bar进行播放进度调节
点击drawer layout中的登出进行账号登出并停止服务
通知栏对音乐播放的控制、在通知栏展示歌名、监听音乐状态的变化

**暂未解决**

在非联网状态下播放音乐会直接闪退
暂不支持本地音乐播放
搜索功能暂未实现
没有做邮箱登录和注册
没有做更改各种信息
只能通过任务管理清除销毁任务
