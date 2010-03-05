hyk-proxy 0.2.0  Read Me
Release 2010/01/31
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
along with GAppProxy.  If not, see <http://www.gnu.org/licenses/>.

Dependencies
------------
1. You need to install JRE/JDK(5+).
2. You need to install Google App Engine SDK(Java) (developed under 1.3.0, don't test earlier version)

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
    
GAE client part: （GAE client部分）
  1. unzip hyk-proxy-client-[version].zip
    任意目录下解压hyk-proxy-client-[version].zip
  2. cd hyk-proxy-client-[version] 
    进入解压的目录
  3. modify etc/hyk-proxy-client.properties, refer the comment for more information
    参照注释修改etc/hyk-proxy-client.properties
  4. execute bin/start.bat(start.sh) to start the local server, execute bin/stop.bat to stop it
    执行bin/start.bat(start.sh)启动local server，bin/stop.bat(stop.sh)停止

 