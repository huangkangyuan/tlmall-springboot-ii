package fun.sherman.tlmall.util;


import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * FTP服务器工具类封装
 *
 * @author sherman
 */
public class FtpUtil {
    private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    private static final String REMOTE_PATH = "img";
    private static final int FTP_PORT = 21;

    private String ip;
    private int port;
    private String user;
    private String password;
    private FTPClient ftpClient;

    public FtpUtil(String ip, int port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public static boolean uploadFile(List<File> fileList) {
        FtpUtil ftpUtil = new FtpUtil(ftpIp, FTP_PORT, ftpUser, ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result = uploadFile(REMOTE_PATH, fileList);
        logger.info("结束上传，上传结果：{}", result);
        return result;
    }

    public static boolean uploadFile(String remotePath, List<File> fileList) {
        FtpUtil ftpUtil = new FtpUtil(ftpIp, FTP_PORT, ftpUser, ftpPass);
        return ftpUtil.uploadFileInternal(remotePath, fileList);
    }

    private boolean uploadFileInternal(String remotePath, List<File> fileList) {
        boolean uploaded = true;
        FileInputStream fis = null;
        // 连接ftp服务器
        if (connectServer(ip, user, password)) {
            try {
                // 文件必须存在
                ftpClient.makeDirectory(remotePath);
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File file : fileList) {
                    fis = new FileInputStream(file);
                    ftpClient.storeFile(file.getName(), fis);
                }
            } catch (IOException e) {
                logger.error("上传文件失败", e);
                uploaded = false;
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("关闭资源失败", e);
                }
            }
        }
        return uploaded;
    }

    private boolean connectServer(String ip, String ftpUser, String ftpPass) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(ftpUser, ftpPass);
        } catch (IOException e) {
            logger.error("连接ftp服务器失败", e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
