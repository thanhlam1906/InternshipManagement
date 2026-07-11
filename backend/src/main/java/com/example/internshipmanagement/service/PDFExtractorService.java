package com.example.internshipmanagement.service;

import com.example.internshipmanagement.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Service
public class PDFExtractorService {

    // PDF magic bytes: %PDF (hex: 25 50 44 46)
    private static final byte[] PDF_MAGIC_BYTES = { 0x25, 0x50, 0x44, 0x46 };

    // Security limits
    private static final int MAX_PDF_PAGES = 10;
    private static final int MAX_EXTRACTED_TEXT_LENGTH = 15_000;

    /**
     * Extract raw text content from a PDF file in-memory.
     * Includes security validations:
     * - File not empty
     * - Extension check (.pdf)
     * - Magic bytes validation (prevents disguised files)
     * - Page count limit (max 10 pages)
     * - Text length truncation (max 15,000 chars)
     *
     * No file is saved to disk.
     *
     * @param file the uploaded PDF file
     * @return extracted text content (truncated if necessary)
     */
    public String extractText(MultipartFile file) {
        // 1. Basic null/empty check
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File PDF khong duoc de trong");
        }

        // 2. Extension check
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Chi chap nhan file PDF");
        }

        try {
            byte[] fileBytes = file.getBytes();

            // 3. Magic bytes validation — prevent disguised files
            validateMagicBytes(fileBytes);

            try (PDDocument document = Loader.loadPDF(fileBytes)) {

                // 4. Page count limit
                int pageCount = document.getNumberOfPages();
                if (pageCount > MAX_PDF_PAGES) {
                    throw new IllegalArgumentException(
                            String.format("File PDF co %d trang, vuot qua gioi han %d trang. CV chi nen co 1-3 trang.",
                                    pageCount, MAX_PDF_PAGES));
                }

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text == null || text.isBlank()) {
                    throw new ExternalApiException(
                            "Khong the trich xuat noi dung tu file PDF. File co the la anh scan.");
                }

                text = text.trim();

                // 5. Text length truncation
                if (text.length() > MAX_EXTRACTED_TEXT_LENGTH) {
                    log.warn("CV text truncated: {} -> {} characters from file '{}'",
                            text.length(), MAX_EXTRACTED_TEXT_LENGTH, originalFilename);
                    text = text.substring(0, MAX_EXTRACTED_TEXT_LENGTH);
                }

                log.info("PDF text extracted successfully: {} characters, {} pages from file '{}'",
                        text.length(), pageCount, originalFilename);
                return text;
            }
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new ExternalApiException("Loi khi doc file PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Validate that the file contains PDF magic bytes (%PDF) within the first 1024 bytes.
     * Per PDF specification, the %PDF header can appear anywhere within the first 1024 bytes.
     */
    private void validateMagicBytes(byte[] fileBytes) {
        if (fileBytes.length < PDF_MAGIC_BYTES.length) {
            throw new IllegalArgumentException("File khong hop le: qua nho de la file PDF");
        }

        // Search for %PDF in the first 1024 bytes
        int searchLimit = Math.min(fileBytes.length - PDF_MAGIC_BYTES.length + 1, 1024);
        boolean found = false;
        for (int i = 0; i < searchLimit; i++) {
            if (fileBytes[i] == 0x25 &&       // %
                fileBytes[i + 1] == 0x50 &&   // P
                fileBytes[i + 2] == 0x44 &&   // D
                fileBytes[i + 3] == 0x46) {   // F
                found = true;
                break;
            }
        }

        if (!found) {
            // Log the first few bytes as hex for diagnostics if validation fails
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(fileBytes.length, 16); i++) {
                sb.append(String.format("%02X ", fileBytes[i]));
            }
            log.warn("PDF magic byte validation failed. First 16 bytes: {}", sb.toString());
            
            throw new IllegalArgumentException(
                    "File khong phai la PDF hop le. Vui long upload dung dinh dang PDF.");
        }
    }
}
