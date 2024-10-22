package caselab.domain.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorage {

    String put(MultipartFile obj);

    InputStream get(String url);

    boolean delete(String url);

}
