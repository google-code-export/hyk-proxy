/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hyk.proxy.framework.shell.gui;

import com.hyk.proxy.framework.appdata.AppData;
import com.hyk.proxy.framework.common.Constants;
import com.hyk.proxy.framework.util.ImageUtil;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class SysTray {

    private void restoreFrame(MainFrame fm)
    {
         //fm.setAlwaysOnTop(true);
         fm.setState(Frame.MAXIMIZED_BOTH);
         fm.setVisible(true);
    }

    public SysTray(final MainFrame mainFrame) throws AWTException {
        final SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(ImageUtil.DONK.getImage(),
                Constants.PROJECT_NAME, popup);
        trayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                restoreFrame(mainFrame);
            }
        });

        MenuItem item = new MenuItem("Restore");
        item.setFont(new Font(null, Font.BOLD, 12));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               restoreFrame(mainFrame);
            }
        });
        popup.add(item);

        item = new MenuItem("View Log");
        item.setFont(new Font(null, Font.BOLD, 12));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(AppData.getLogHome().toURI());
                } catch (IOException ex) {
                    Logger.getLogger(SysTray.class.getName()).log(Level.SEVERE, null, ex);
                }
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
