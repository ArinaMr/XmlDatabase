package org.ispirer.xmldatabase.controller;

import lombok.RequiredArgsConstructor;
import org.ispirer.xmldatabase.service.XmlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/xml")
public class XmlController {
    private final XmlService xmlService;

    @PostMapping("/import")
    @ResponseBody
    public ResponseEntity<String> importXml(@RequestParam("file") MultipartFile file) {
        try {
            File tmp = File.createTempFile("upload", ".xml");
            file.transferTo(tmp);

            xmlService.importXml(tmp);

            return ResponseEntity.ok("XML imported successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to import XML: " + e.getMessage());
        }
    }

    @GetMapping("/export/{id}")
    @ResponseBody
    public ResponseEntity<String> exportXml(@PathVariable Long id) {
        try {
            String xml = xmlService.exportXml(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .body(xml);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to export XML: " + e.getMessage());
        }
    }
}
