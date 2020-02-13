package fun.sherman.tlmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author sherman
 */
public interface IFileService {
    String upload(MultipartFile multipartFile, String path);
}
