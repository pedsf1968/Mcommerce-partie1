package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.*;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Api(description = "API pour les opérations CRUD sur les produits.")
//@Api(tags = {"API pour les opérations CRUD sur les produits."})
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

    public Product afficherUnProduit(@PathVariable int id) throws ProduitIntrouvableException {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est introuvable !");

        return produit;
    }

    /**
     * rechercheProduitParSonNom : Recherche les produits dont le nom contient un texte
     * GET /Produits/recherche/{texte}
     *
     * @param texte du produit recherché
     * @return List<Product> la liste des produits trouvés
     */
    @ApiOperation(value = "Recherche les produits dont le nom contient un texte")
    @GetMapping(value = "/Produits/recherche/{texte}")
    public List<Product> rechercheProduitParSonNom(@PathVariable String texte){
        return productDao.findByNomLike("%" + texte +"%");
    }

    /**
     * ajouterProduit : Ajoute un produit dans la base de données
     * POST /Produits
     *
     * @param product récupéré dans le body
     * @return ResponseEntity
     */
    @ApiOperation(value = "Ajoute un produit dans la base de données")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        if(productDao.findById(product.getId())!=null)
           throw new ProduitExistantException("On ne peut pas dupliquer un produit !");

        if(product.getPrix()==0)
            throw new ProduitGratuitException("On ne peut pas donner un produit !");

        if(product.getPrix()<product.getPrixAchat())
            throw new ProduitVenteAPerteException("On ne peut vendre un produit à perte !");

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
    public ResponseEntity<Void> supprimerProduit(@PathVariable int id) {

        if(productDao.findById(id) == null)
            throw new ProduitIntrouvableException("On ne peut pas supprimer un produit qui n'existe pas !");

        productDao.delete(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * updateProduit : Modifie les données relative à un produit identifié par son ID
     * PUT /Produits
     *
     * @param product récupéré dans le body
     */
    @ApiOperation(value = "Modifie les données relative à un produit identifié par son ID")
    @PutMapping (value = "/Produits")
    public ResponseEntity<Void> updateProduit(@RequestBody Product product) {

        if(productDao.findById(product.getId()) == null)
            throw new ProduitIntrouvableException("On ne peut pas supprimer un produit qui n'existe pas !");

        if(product.getPrix()==0)
            throw new ProduitGratuitException("On ne peut pas donner un produit !");

        if(product.getPrix()<product.getPrixAchat())
            throw new ProduitVenteAPerteException("On ne peut vendre un produit à perte !");

        productDao.save(product);

        return ResponseEntity.accepted().build();
    }

    /**
     * calculerMargeProduit : Récupère la liste des produits et leur marge sous forme de Map<String,Integer>
     * GET /AdminProduits
     * @return Map<String,Integer> liste des produits et de leur marge
     */
    @ApiOperation(value = "Récupère la liste des produits et leur marge sous forme de Map<String,Integer>")
    @GetMapping(value = "/AdminProduits")
    public ResponseEntity<Map<String,Integer>> calculerMargeProduit(){
        Map<String,Integer> mapProduits = new TreeMap<>();

        for(Product p: productDao.findAll()) {
            mapProduits.put(p.toString(), p.getPrix() - p.getPrixAchat());
        }

        return new ResponseEntity<>(mapProduits, HttpStatus.CREATED);
    }

    /**
     * listProduitsTrieParNom : Renvoie une liste des produits trié par ordre alphabétique
     * GET /Produits/triparnom
     *
     * @return List des produits triés
     */
    @ApiOperation(value="Renvoie une liste des produits trié par ordre alphabétique")
    @GetMapping(value = "/Produits/triparnom")
    public ResponseEntity<List<Product>> listProduitsTrieParNom(){

        return new ResponseEntity<>(productDao.findAllByOrderByNomAsc(),HttpStatus.OK);
    }

    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public ResponseEntity<List<Product>>  testeDeRequetes(@PathVariable int prix) {

        return new ResponseEntity<>(productDao.chercherUnProduitCher(400), HttpStatus.FOUND);
    }



}
