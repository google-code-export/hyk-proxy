hyk-proxy 0.8.6  Read Me
Release 2010/06/16
http://hyk-proxy.googlecode.com 

This file is part of hyk-proxy.                                   
                                                                  
hyk-proxy is free software: you can redistribute it and/or modify 
it under the terms of the GNU General Public License as           
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.                   
                                                                  
hyk-proxy is distributed in the hope that it will be useful,      
but WITHOUT ANY WARRANTY; without even the implied warranty of    
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the     
GNU General Public License for more details.                      
                                                                  
You should have received a copy of the GNU General Public License 
along with hyk-proxy(client & server & plugin).  If not, see <http://www.gnu.org/licenses/>.

Dependencies
------------
1. You need to install JRE/JDK(1.6+).
2. You need to install Google App Engine SDK(Java) (use the latest version)
3. You may need intall seattle SDK if you want to use seattle platform

INSTALL:
GAE server part：（GAE server部分）
 1. unzip hyk-proxy-server-[version].zip
    任意目录下解压hyk-proxy-server-[version].zip
 2. cd hyk-proxy-server-[version] 
    进入解压的目录
 3. modify war/WEB-INF/appengine-web.xml, change the element '<application>hyk-proxy-demo</application>'
    修改war/WEB-INF/appengine-web.xml， 将'<application>'值改为自己创建的appid
 4. execute appcfg update (make sure you are in the directory 'hyk-proxy-server-[version]')
    执行appcfg.cmd/appcfg.sh update war上传

Seattle server part:
 1. Login seattle
 2. apply some resources
 3. run your application
    
Client part: （Client部分）
  1. unzip hyk-proxy-client-[version].zip
    任意目录下解压hyk-proxy-client-[version].zip
  2. cd hyk-proxy-client-[version] 
    进入解压的目录
  3. modify etc/hyk-proxy-client.conf.xml, refer the comment for more information
    参照注释修改etc/hyk-proxy-client.conf.xml
  4. execute bin/start.bat(start.sh) to start the local server, execute bin/stop.bat to stop it
    执行bin/start.bat(start.sh)启动local server，bin/stop.bat(stop.sh)停止

 
