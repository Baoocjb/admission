package com.gdut.admission.controller;

import com.gdut.admission.dto.Result;
import com.gdut.admission.service.IAdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admission")
public class AdmissionController {

    @Autowired
    private IAdmissionService admissionService;

    @GetMapping("startAd")
    public Result admission(){
        return admissionService.admission();
    }
}
