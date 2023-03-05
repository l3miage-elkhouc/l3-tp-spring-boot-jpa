package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
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
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    private final AuthorMapper authorMapper;
    private final AuthorService authorService;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper , AuthorMapper authorMapper , AuthorService authorService) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorMapper = authorMapper;
        this.authorService = authorService;
    }

    // Renvoi tous les livres
    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) String query) {
        Collection<Book> books;
        if (query == null) {
            books = bookService.list();
        } else {
            books = bookService.findByTitle(query);
        } 
        return books.stream()
                .map(booksMapper::entityToDTO)
                .toList();
    }

    // Renvoi le livre dont l'id est passé en paramètre
    @GetMapping("/books/{id}")
    @ResponseStatus(HttpStatus.OK) // code réponse 200 (OK)
    public BookDTO book(@PathVariable("id") Long id) {
        try{
            Book book = bookService.get(id);
            return booksMapper.entityToDTO(book);
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND ,"The book was not found");
        } 
    }

    
    // Créer un nouveau livre pour un auteur donné en paramètre
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED) // code réponse 201
    public BookDTO newBook(Long authorId, @RequestBody @Valid BookDTO book)  {
            Book livre = booksMapper.dtoToEntity(book);
            try {
                Book saved = this.bookService.save(authorId, livre);
                return booksMapper.entityToDTO(saved);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"The author was not found");
            }
    }


    //Mis à jour d'un livre 
    @PutMapping("/books/{id}")
    public BookDTO updateBook( @PathVariable("id") Long bookId, @RequestBody @Valid BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
            if ( book.id() != bookId) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            Book livre = this.booksMapper.dtoToEntity(book) ;
            try{
                Book updated = this.bookService.update(livre);
                return this.booksMapper.entityToDTO(updated);
            } catch (Exception e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"The book was not found");
            }
    }

    // Supression d'un livre
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // code réponse 204
    public void deleteBook(@PathVariable("id") Long id) {
        try{
            bookService.delete(id);
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book was not found");
        }
    }


    // Ajout d'un auteur supplémentaire à un livre
    @PutMapping("/books/{bookId}/authors")
    public BookDTO addAuthor(@PathVariable("bookId") Long bookId , @RequestBody @Valid AuthorDTO author) {
            try{
                Book upd = this.bookService.addAuthor(bookId, author.id());
                return booksMapper.entityToDTO(upd);
            } catch (EntityNotFoundException e){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"The book was not found");
            }
    }


}
