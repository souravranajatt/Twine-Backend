package com.loginapp.loginapp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    // ✅ Image/Video Upload
    public String uploadFile(byte[] fileBytes, String fileName, String contentType) throws IOException {
        
        String resourceType = contentType.startsWith("video/") ? "video" : "image";

        Map<?, ?> result = cloudinary.uploader().upload(
            fileBytes,
            ObjectUtils.asMap(
                "public_id",     "twine/posts/" + fileName,
                "resource_type", resourceType,
                "overwrite",     true
            )
        );
        return result.get("secure_url").toString();
    }

    // ✅ File Delete
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}