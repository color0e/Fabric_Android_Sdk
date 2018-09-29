# Fabric_Android_Sdk

## 사용법
### 프로젝트 루트
<pre><code>mvn clean</code></pre>
<pre><code>mvn install -DskipTests</code></pre>
<pre><code>결과물:Fabric_Android_Sdk/target/fabric-sdk-java-1.1.0-alpha.jar</code></pre>

### Fabric Helper
#### ※필독사항:Fabrichelper.java에서 grpc 주소부분을 자신이 테스트하는 서버아이피로 변경
<pre><code>위치:Fabric_Android_Sdk/src/main/java/org/hyperledger/fabric/sdk/Fabrichelper.java</code></pre>
<pre><code>HFCAClient caClient = getHfCaClient("http://Fabric_ca_ip_address:7054", null);</code></pre>
<pre><code>AppUser admin = getAdmin(caClient);</code></pre>
<pre><code>log.info(admin);</code></pre>
<pre><code>AppUser appUser = getUser(caClient, admin, "hfuser");</code></pre>
<pre><code>log.info(appUser);</code></pre>
<pre><code>HFClient client = getHfClient();</code></pre>
<pre><code>client.setUserContext(admin);</code></pre>
<pre><code>Channel channel = getChannel(client);</code></pre>
<pre><code>log.info("Channel: " + channel.getName());</code></pre>
<pre><code>String[] iargs=new String[]{"CAR10","WOW","WOW","WOW","WOW"};</code></pre>
<pre><code>String[] qargs=new String[]{"null"};</code></pre>
<pre><code>invokeBlockChain(client,iargs);</code></pre>
<pre><code>String qresult=queryBlockChain(client,qargs);</code></pre>
<pre><code>System.out.println("query result:"+qresult);</code></pre>

### Reference
https://github.com/shubhamvrkr/android-fabric-sdk
