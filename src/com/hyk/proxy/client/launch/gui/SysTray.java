/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hyk.proxy.client.launch.gui;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Administrator
 */
public class SysTray
{
    
    public SysTray(final GUILauncher mainFrame) throws AWTException
    {
        final SystemTray tray = SystemTray.getSystemTray();
         PopupMenu popup = new PopupMenu();
         final TrayIcon      trayIcon = new TrayIcon(ImageUtil.GAE.getImage(),
                        "hyk-proxy-client", popup);
         trayIcon.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                        mainFrame.setVisible(true);
                        //mainFrame.setl
                    }
          });

                MenuItem item = new MenuItem("Restore");
                item.setFont(new Font(null, Font.BOLD, 12));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mainFrame.setVisible(true);
                    }
                });

                popup.add(item);

                item = new MenuItem("Exit");
                item.setFont(new Font(null, Font.BOLD, 12));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mainFrame.exit();
                    }
                });
                popup.add(item);
                tray.add(trayIcon);
    }
}
