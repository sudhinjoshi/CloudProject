package com.nci.prj.repositories;

import com.nci.prj.model.Products;
import org.springframework.data.repository.CrudRepository;

/**
 * Interface ProductRepository
 * <p>
 *
 * @author Sudhindra Joshi
 */
public interface ProductRepository extends CrudRepository<Products, String> {

    Products findByProdName(String product);
}
