package com.nci.prj.controller;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.nci.prj.model.Products;
import com.nci.prj.model.Role;
import com.nci.prj.model.myUser;
import com.nci.prj.repositories.ProductRepository;
import com.nci.prj.repositories.S3Services;
import com.nci.prj.services.CustomUserDetailsService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    S3Services s3Services;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static String UPLOADED_FOLDER = "/";
    private AmazonS3 s3;

    @RequestMapping(path = "/download/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(Model model, HttpServletRequest request, @PathVariable String fileName) throws IOException {

        byte[] content = null;
        System.out.println("Inside download : " + fileName);
        //return s3Services.downloadFile(fileName);

        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion("us-east-1")
                .build();

        // List current buckets.
        ListMyBuckets();

        final S3Object s3Object = s3.getObject(bucketName, fileName);
        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();

        byte[] bytes = IOUtils.toByteArray(objectInputStream);

        String fileName11 = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName11);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);

    }


    @RequestMapping(value = "/productCreation", method = RequestMethod.POST)
    public ModelAndView productCreationPost(@RequestParam String prodName, @RequestParam String prodDesc, @RequestParam float prodPrice,
                                            @RequestParam int prodQuantity, @RequestParam("prodImage") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {
        System.out.println("productCreation POST 1: " + prodName);
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        Products product = new Products();
        product.setProdName(prodName);
        product.setProdDesc(prodDesc);
        product.setProdPrice(prodPrice);

        String fileupload = "";

        try {
            if (file.isEmpty()) {
                product.setProdUrl("");
                System.out.println("Specification is not posted");
                //redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
                //modelAndView.addObject("successMessage", "Product has been registered successfully. However file was empty");
            } else {
                product.setProdUrl(file.getOriginalFilename());
                final File newfile = convertMultiPartFileToFile(file);

                s3 = AmazonS3ClientBuilder.standard()
                        .withCredentials(new ProfileCredentialsProvider())
                        .withRegion("us-east-1")
                        .build();

                // List current buckets.
                ListMyBuckets();

                //upload the file
                s3.putObject(bucketName, file.getOriginalFilename(), newfile);

            }
        } catch (Exception e) {
            e.printStackTrace();
            fileupload = "However Specification uploading failed. Try Edit Product later";
        }

        product.setProdQuantity(prodQuantity);
        String prodId = productRepository.save(product).getId();
        System.out.println("New Product saved: Id: " + prodId);
        modelAndView.addObject("successMessage", "Product has been registered successfully. " + fileupload);
        modelAndView.addObject("products", productRepository.findAll());

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        modelAndView.setViewName("listProduct");

        return modelAndView;
    }

    private void ListMyBuckets() {
        List<Bucket> buckets = s3.listBuckets();
        System.out.println("My buckets now are:");

        for (Bucket b : buckets) {
            System.out.println(b.getName());
            ListObjectsV2Result result = s3.listObjectsV2(b.getName());
            List<S3ObjectSummary> objects = result.getObjectSummaries();
            for (S3ObjectSummary os : objects) {
                System.out.println("** " + os.getKey());
            }
        }
    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
            System.out.println("Error converting the multi-part file to file= " + ex.getMessage());
        }
        return file;
    }

    @RequestMapping(value = "/productCreation", method = RequestMethod.GET)
    public ModelAndView productCreationGet() {
        System.out.println("productCreation: GET ");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        modelAndView.addObject("successMessage", "Product has been registered successfully");
        modelAndView.addObject("products", new Products());
        modelAndView.setViewName("createProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/productList", method = RequestMethod.GET)
    public ModelAndView productList(Model model) {
        System.out.println("productList: GET ");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        //modelAndView.addObject("successMessage", "Product has been listed successfully");
        modelAndView.addObject("products", productRepository.findAll());

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        //model.addAttribute("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/productListUser", method = RequestMethod.GET)
    public ModelAndView productListUser(Model model) {
        System.out.println("productListUser: GET ");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        //modelAndView.addObject("successMessage", "Product has been listed successfully");
        modelAndView.addObject("products", productRepository.findAll());

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        //modelAndView.addObject("userisadmin", true);
        //model.addAttribute("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/productInventory", method = RequestMethod.GET)
    public ModelAndView productInventory(Model model) {
        System.out.println("productInventory: GET ");
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("auth: " + auth.getName());
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);
        //modelAndView.addObject("successMessage", "Product has been listed successfully");
        modelAndView.addObject("products", productRepository.findAll());
        //model.addAttribute("products", productRepository.findAll());
        modelAndView.setViewName("editInventory");

        return modelAndView;
    }

    @RequestMapping(value = "/productInventory", method = RequestMethod.POST)
    public ModelAndView productInventoryUpdate(@RequestParam String productId, @RequestParam int newProdQty) {
        Optional<Products> product = productRepository.findById(productId);
        product.get().setProdQuantity(newProdQty);
        productRepository.save(product.get());

        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        modelAndView.addObject("successMessage", "Product Quantity has been updated successfully");
        modelAndView.addObject("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/productShow/{id}", method = RequestMethod.GET)
    public ModelAndView productShow(Model model, @PathVariable String id) {
        System.out.println("productShow: " + id);
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        modelAndView.addObject("product", productRepository.findById(id).get());
        modelAndView.addObject("productDescription", productRepository.findById(id).get().getProdDesc());
        modelAndView.setViewName("showProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/productdelete")
    public ModelAndView delete(@RequestParam String id) {

        Optional<Products> product = productRepository.findById(id);
        String fileName = product.get().getProdUrl();
        System.out.println("Inside Delete: " + fileName);
        productRepository.delete(product.get());

        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        modelAndView.addObject("successMessage", "Product has been deleted successfully");
        modelAndView.addObject("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion("us-east-1")
                .build();

        // List current buckets.
        ListMyBuckets();

        s3.deleteObject(bucketName, fileName);

        return modelAndView;
    }

    @RequestMapping(value = "/productedit/{id}")
    public ModelAndView edit(@PathVariable String id, Model model) {

        Optional<Products> product = productRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        modelAndView.addObject("successMessage", "Product has been updated successfully");
        modelAndView.addObject("product", productRepository.findById(id).get());
        modelAndView.setViewName("editProduct");

        return modelAndView;
    }

    @RequestMapping(value = "/editProduct", method = RequestMethod.POST)
    public ModelAndView update(@RequestParam String id, @RequestParam String prodName, @RequestParam String prodDesc, @RequestParam float prodPrice, @RequestParam String prodUrl, @RequestParam int prodQty) {
        Optional<Products> product = productRepository.findById(id);
        product.get().setProdName(prodName);
        product.get().setProdDesc(prodDesc);
        product.get().setProdPrice(prodPrice);
        product.get().setProdUrl(prodUrl);
        product.get().setProdQuantity(prodQty);
        productRepository.save(product.get());

        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        for (Role chkRole : user.getRoles()) {
            System.out.println("chkRole: " + chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        modelAndView.addObject("successMessage", "Product has been updated successfully");
        modelAndView.addObject("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        return modelAndView;

    }

}


