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
    public ResponseEntity<String> exportXml(@PathVariable String id) {
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

    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<String> exportXml(
            @RequestParam String name,
            @RequestParam(required = false) String schema) {
        try {
            String xml = xmlService.exportXml(name, schema);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .body(xml);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to export XML: " + e.getMessage());
        }
    }

    @PostMapping("/transform")
    public ResponseEntity<String> transformXml(
            @RequestParam("xmlFile") MultipartFile xmlFile,
            @RequestParam("xsltFile") MultipartFile xsltFile,
            @RequestParam("externalFile") MultipartFile externalFile) {
        try {
            String result = xmlService.xsltTransform(xmlFile, xsltFile, externalFile);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to transform XML: " + e.getMessage());
        }
    }
}
