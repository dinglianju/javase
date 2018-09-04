package com.dlj.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.dlj.bean.ResultBean;

@Controller
public class FileUpload {
	private static final Logger log = LoggerFactory.getLogger(FileUpload.class);
	
	@RequestMapping("fileUplad")
	public String fileUpload(@RequestParam(value = "file", required = false) MultipartFile file,
			HttpServletRequest request) {
		if (!file.isEmpty()) {
			try {
				String filePath = request.getSession().getServletContext().getRealPath("/") + "fileUpload/temp/"
						+ file.getOriginalFilename();
				file.transferTo(new File(filePath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "redirect:/list";
	}

	@RequestMapping("list")
	public ModelAndView list(HttpServletRequest request) {
		String filePath = request.getSession().getServletContext().getRealPath("/") + "fileUpload/temp/";
		ModelAndView view = new ModelAndView("list");
		File uploadDest = new File(filePath);
		String[] fileNames = uploadDest.list();
		for (int i = 0; i < fileNames.length; i++) {
			System.out.println(fileNames[i]);
		}
		return view;
	}

	@ResponseBody
	@RequestMapping(value = "multiUpload", method = RequestMethod.POST, consumes = {"multipart/form-data"}, produces = {"application/json;charset=UTF-8"})
	public ResultBean<Boolean> multiFileUpload(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException {
		//if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator<String> it = multiRequest.getFileNames();
			while (it.hasNext()) {
				String filename = it.next();
				MultipartFile file = multiRequest.getFile(filename);
				String fileName = file.getOriginalFilename();
				String physicalPath = request.getSession().getServletContext().getRealPath("/") + "fileUpload/temp/" + fileName;
				saveFile(file, physicalPath);
			}
		//}
		return new ResultBean<Boolean>(true);
	}
	
	@RequestMapping("/filesUpload")  
    public String filesUpload(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) throws IllegalStateException, IOException { 
        //判断file数组不能为空并且长度大于0  
        if(files!=null&&files.length>0){
            //循环获取file数组中得文件  
            for(int i = 0;i<files.length;i++){  
                MultipartFile file = files[i];  
                String fileName = file.getOriginalFilename();
    			String physicalPath = request.getSession().getServletContext().getRealPath("/") + "fileUpload/temp/" + fileName;
				//保存文件  
                saveFile(file, physicalPath);  
            }  
        }  
        // 重定向  
        return "redirect:/list";  
    }
	
	private void saveFile(MultipartFile file, String path) throws IllegalStateException, IOException {
		if (file != null) {
			String fn = file.getName();
			String fileName = file.getOriginalFilename();
			long fileSize = file.getSize();
			log.info("name: {}, filename: {}, fileSize: {}", fn, fileName, fileSize);
			File newFile = new File(path);
			file.transferTo(newFile);
		}
	}
}
