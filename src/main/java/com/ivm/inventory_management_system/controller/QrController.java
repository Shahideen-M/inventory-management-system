package com.ivm.inventory_management_system.controller;

import com.ivm.inventory_management_system.service.QrService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping("/owner/{ownerId}")
    public String getOwnerQr(@PathVariable Long ownerId) {

        String ownerUrl = "http://localhost:8080/customer/owner/" + ownerId + "/items";

        String qrBase64 = qrService.generateQrCode(ownerUrl, 300, 300);

        return "data:image/png;base64," + qrBase64;
    }
}
