package com.nci.prj.controller;

import com.nci.prj.model.Products;
import com.nci.prj.model.Role;
import com.nci.prj.model.myUser;
import com.nci.prj.repositories.ProductRepository;
import com.nci.prj.repositories.S3Services;
import com.nci.prj.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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

    @RequestMapping(path = "/download/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(Model model, HttpServletRequest request, @PathVariable String fileName) throws IOException {

        return s3Services.downloadFile(fileName);

        /*
        Resource resource;
        Path path = Paths.get(UPLOADED_FOLDER + fileName);

        System.out.println("path: " + path.getFileName().toAbsolutePath());

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

         */

    }


    @RequestMapping(value = "/productCreation", method = RequestMethod.POST)
    public ModelAndView productCreationPost(@RequestParam String prodName, @RequestParam String prodDesc, @RequestParam float prodPrice,
                                            @RequestParam int prodQuantity, @RequestParam("prodImage") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {
        System.out.println("productCreation POST: " + prodName);
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
                s3Services.uploadFile(file.getOriginalFilename(), newfile);

                /*
                // Get the file and save it somewhere
                byte[] bytes = file.getBytes();

                Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
                System.out.println("+1: " + path.toAbsolutePath());

                Files.write(path, bytes);
                */
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
        modelAndView.setViewName("listProduct");

        return modelAndView;
    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
            System.out.println("Error converting the multi-part file to file= "+ex.getMessage());
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

        for(Role chkRole : user.getRoles()){
            System.out.println("chkRole: "+chkRole);
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

        for(Role chkRole : user.getRoles()){
            System.out.println("chkRole: "+chkRole);
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
        productRepository.delete(product.get());
        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

        modelAndView.addObject("successMessage", "Product has been deleted successfully");
        modelAndView.addObject("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        for(Role chkRole : user.getRoles()){
            System.out.println("chkRole: "+chkRole);
            if (chkRole.getRole().equalsIgnoreCase("admin")) {
                modelAndView.addObject("userisadmin", true);
            }
        }

        return modelAndView;
    }

    @RequestMapping(value = "/productedit/{id}")
    public ModelAndView edit(@PathVariable String id, Model model) {

        Optional<Products> product = productRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        myUser user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("user", user);

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

        modelAndView.addObject("successMessage", "Product has been updated successfully");
        modelAndView.addObject("products", productRepository.findAll());
        modelAndView.setViewName("listProduct");

        return modelAndView;

    }

}

