package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Api(tags = {"API pour es opérations CRUD sur les produits."})
@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    /**
     * listeProduit : Récupérer la liste des produits en filtrant certains champs
     * GET /Produits
     *
     * @return MappingJacksonValue liste des produits filtré
     */
    @ApiOperation(value = "Récupère la liste des produits en filtrant certains champs")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    /**
     * afficherUnProduit : Récupérer un produit grâce à son ID à condition que celui-ci soit en stock
     * GET /Produits/{id}
     *
     * @param id identifiant récupéré dans l'URL
     * @return produit trouvé
     */
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }

    /**
     * rechercheProduitParSonNom : Recherche un produit par son nom
     * GET /Produits/nom/{nom}
     *
     * @param nom du produit recherché
     * @return List<Product> la liste des produits trouvés
     */
    @ApiOperation(value = "Recherche un produit par son nom")
    @GetMapping(value = "/Produits/nom/{nom}")
    public List<Product> rechercheProduitParSonNom(@PathVariable String nom){
        return productDao.findByNomLike(nom);
    }

    //ajouter un produit
    @PostMapping(value = "/Produits")

    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        Product productAdded =  productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    /**
     * supprimerProduit : Supprime un produit identifié par son ID de la base de données
     * DELETE /Produits/{id}
     *
     * @param id identifiant récupéré dans l'URL
     */
    @ApiOperation(value = "Supprime un produit identifié par son ID de la base de données")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    /**
     * updateProduit : Modifie les données relative à un produit identifié par son ID
     * PUT /Produits
     *
     * @param product récupéré dans le body
     */
    @ApiOperation(value = "Modifie les données relative à un produit identifié par son ID")
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

    /**
     * calculerMargeProduit : Récupère la liste des produits et leur marge sous forme de Map<String,Integer>
     * GET /AdminProduits
     * @return Map<String,Integer> liste des produits et de leur marge
     */
    @ApiOperation(value = "Récupère la liste des produits et leur marge sous forme de Map<String,Integer>")
    @GetMapping(value = "/AdminProduits")
    public Map<String,Integer> calculerMargeProduit(){
        Map<String,Integer> mapProduits = new TreeMap<>();

        for(Product p: productDao.findAll()) {
            mapProduits.put(p.toString(), p.getPrix() - p.getPrixAchat());
        }

        return mapProduits;
    }

    /**
     * listProduitsTrieParNom : Renvoie une liste des produits trié par ordre alphabétique
     * GET /Produits/triparnom
     *
     * @return List des produits triés
     */
    @ApiOperation(value="renvoie une liste des produits trié par ordre alphabétique")
    @GetMapping(value = "/Produits/triparnom")
    public List<Product> listProduitsTrieParNom(){
        return productDao.listProduitsAlphabetic();
    }

    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }



}
