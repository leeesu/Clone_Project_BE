package com.example.intermediate.external;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.intermediate.external.dto.S3Component;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AwsS3UploadService implements UploadService {

    private final AmazonS3 s3Client;
    private final S3Component component;  // AWS S3 를 위한 설정이 담긴 클래스

    public List<String> upload(List<MultipartFile> multipartFile) {
        List<String> imgUrlList = new ArrayList<>();

        // forEach 구문을 통해 multipartFile로 넘어온 파일들 하나씩 fileNameList에 추가
        for (MultipartFile file : multipartFile) {
            String fileName = createFileName(file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try(InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(new PutObjectRequest(component.getBucket(), fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
                imgUrlList.add(s3Client.getUrl(component.getBucket(), fileName).toString());
            } catch(IOException e) {
                throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생하였습니다 (%s)", file.getOriginalFilename()));
            }
        }
        return imgUrlList;
    }


    // Amazon S3 를 사용해서 파일 업로드
    @Override
    public void uploadFile(InputStream inputStream, ObjectMetadata objectMetadata, String fileName) {
        s3Client.putObject(new PutObjectRequest(component.getBucket(), fileName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    // 업로드한 파일의 Url 가져오는
    @Override
    public String getFileUrl(String fileName) {
        return s3Client.getUrl(component.getBucket(), fileName).toString();
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        try {
            s3Client.deleteObject("mygreensparta", (fileName).replace(File.separatorChar, '/'));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    // 파일 이름 생성 (이름 중복 방지 목적)
    private String createFileName(String originalFileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
    }

    // 파일 유효성 검사
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다", fileName));
        }
    }

    // URL 에서 파일이름(key) 추출
    public static String getFileNameFromURL(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());
    }

}