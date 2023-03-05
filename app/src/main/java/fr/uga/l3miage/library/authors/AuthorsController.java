package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;


@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final BookService bookService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper,BookService bookService) {
        this.authorService = authorService;
        this.bookService = bookService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    // Lit et renvoi un auteur dont l'id est passé en paramètre
    @GetMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO author(@PathVariable("id") Long id) {
        try{
            Author auteur = this.authorService.get(id);
            return this.authorMapper.entityToDTO(auteur);
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND ,"The author was not found" ,e);
        } 
    }

    // créer un nouvel auteur
    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody @Valid AuthorDTO author) {
        Author auteur = authorMapper.dtoToEntity(author);
        auteur = this.authorService.save(auteur);
        return authorMapper.entityToDTO(auteur);
    }

    // Mis à jour d'un auteur qui existe déjà
    @PutMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable("id") Long id) {
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
            if (id != author.id()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            try{

                Author updated = this.authorService.update(this.authorMapper.dtoToEntity(author));
                return this.authorMapper.entityToDTO(updated);
            } catch (EntityNotFoundException e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
    }


    //Suppression d'un auteur 
    @DeleteMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable("id") Long id) {
        try{
            authorService.delete(id);
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The author was not found");
        }
    }

    //Renvoi tous les livres de l'auteur dont l'id est passé en paramètre
    @GetMapping("authors/{id}/books")
    public Collection<BookDTO> books(@PathVariable("id") Long authorId) {
        try {
            Collection<Book> books = bookService.getByAuthor(authorId) ;
            return books.stream().map(booksMapper::entityToDTO).toList();
        } catch(Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    // créer un nouveau livre pour un auteur donné
    @PostMapping("/authors/{id}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable("id") Long authorId,@RequestBody @Valid BookDTO book) {
      
        Book livre = booksMapper.dtoToEntity(book);
        try {
            Book book_aut = this.bookService.save(authorId, livre);
            return booksMapper.entityToDTO(book_aut);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The author was not found");
        }
        
    }

}
