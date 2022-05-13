# jbang-kubectl-plugins

使用jbang开发kubectl插件

### 安装jbang

```bash
scoop bucket add jbangdev https://github.com/jbangdev/scoop-bucket
scoop install jbang
```

### jbang方式运行

```bash
jbang lsimage.java -h
```

输出如下内容:

```
Usage: lsimage [-hV] [-n=<ns>]
list useage image made with jbang
  -h, --help             Show this help message and exit.
  -n, --namespace=<ns>   namespace
  -V, --version          Print version information and exit.
```

```bash
# 默认namespace default
jbang lsimage.java
# 指定 kube-system namespace
jbang lsimage.java -n kube-system
```

### kubectl插件方式运行

进行如下操作之前，首先取消`lsimage.java`源文件中行首`#!/usr/bin/env jbang`的注释

```bash
# 拷贝文件到可执行目录，并进行重命名
cp lsimage.java /usr/local/bin/kubectl-lsimage
# 赋予可执行权限
chmod +x /usr/local/bin/kubectl-lsimage
# 查看kubectl插件列表
kubectl plugin list

# 输出信息如下，由此可见我们使用jbang开发的kubectl插件可以被kubectl识别
# C:\Users\swfeng\.krew\bin\kubectl-access_matrix.exe
# C:\Users\swfeng\.krew\bin\kubectl-df_pv.exe
# C:\Users\swfeng\.krew\bin\kubectl-images.exe
# C:\Users\swfeng\.krew\bin\kubectl-krew.exe
# C:\Users\swfeng\.krew\bin\kubectl-lsimage

# 使用插件
kubectl lsimage # @Command(name = "lsimage")
```

### view Secret 插件

```bash
jbang init --template=cli viewsec.java
jbang viewsec.java
jbang viewsec.java -h
```

在代码内填入业务逻辑即可

# 参考文档

- https://www.jbang.dev
- https://www.jbang.dev/documentation/guide/latest/index.html
- https://dev.to/ikwattro/write-a-kubectl-plugin-in-java-with-jbang-and-fabric8-566

