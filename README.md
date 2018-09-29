# Fabric_Android_Sdk

## 사용법
### 프로젝트 루트
<pre><code>mvn clean</code></pre>
<pre><code>mvn install -DskipTests</code></pre>
<pre><code>결과물:Fabric_Android_Sdk/target/fabric-sdk-java-1.1.0-alpha.jar</code></pre>

### Fabric Helper (package org.hyperledger.fabric.sdk)
#### ※필독사항:Fabrichelper.java에서 서버아이피지정하는 부분을 자신이 테스트하는 서버아이피로 변경
#### 해당 예제는 Fabcar기반으로 테스트한것임 다른체인코드를 테스트해보고 싶다면, Fabrichelper.java 
<pre><code>위치:Fabric_Android_Sdk/src/main/java/org/hyperledger/fabric/sdk/Fabrichelper.java</code></pre>
#### Fabric Ca Client 생성
<pre><code>HFCAClient caClient = getHfCaClient("http://Fabric_ca_ip_address:7054", null);</code></pre>
#### Fabric ca server로 부터 받은 인증서와 개인키를 바탕으로 AppUser인스턴스 생성
<pre><code>AppUser admin = getAdmin(caClient);</code></pre>
<pre><code>log.info(admin);</code></pre>
#### Fabric Client 생성
<pre><code>HFClient client = getHfClient();</code></pre>
#### admin 컨텐스트 설정
<pre><code>client.setUserContext(admin);</code></pre>
#### Channel 객체생성
<pre><code>Channel channel = getChannel(client);</code></pre>
<pre><code>log.info("Channel: " + channel.getName());</code></pre>
#### chaincode 매개변수 설정
<pre><code>String[] iargs=new String[]{"CAR10","WOW","WOW","WOW","WOW"};</code></pre>
<pre><code>String[] qargs=new String[]{"null"};</code></pre>
#### invoke
<pre><code>invokeBlockChain(client,iargs);</code></pre>
#### query
<pre><code>String qresult=queryBlockChain(client,qargs);</code></pre>
<pre><code>System.out.println("query result:"+qresult);</code></pre>

### Reference
https://github.com/shubhamvrkr/android-fabric-sdk
