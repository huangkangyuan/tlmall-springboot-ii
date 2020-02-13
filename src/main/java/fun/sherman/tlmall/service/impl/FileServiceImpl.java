package fun.sherman.tlmall.service.impl;

import com.google.common.collect.Lists;
import fun.sherman.tlmall.service.IFileService;
import fun.sherman.tlmall.util.FtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author sherman
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    private static final String REMOTE_PATH = "img";

    @Override
    public String upload(MultipartFile multipartFile, String path) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extensionName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String newFilename = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24) + "." + extensionName;
        logger.info("开始上传文件，原始文件名：{}, 上传文件路径：{}，新文件名：{}", originalFilename, path, newFilename);
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, newFilename);
        try {
            multipartFile.transferTo(targetFile);
            FtpUtil.uploadFile(REMOTE_PATH, Lists.newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();
    }
}
