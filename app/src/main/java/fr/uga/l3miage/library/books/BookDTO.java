package fr.uga.l3miage.library.books;

import fr.uga.l3miage.library.authors.AuthorDTO;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Collection;

public record BookDTO(
        Long id,
        
        @NotEmpty 
        String title,

        @Min(1000000000)  //@Max(9999999999999)
        long isbn,
        
        String publisher,
        
        @Min(-9999) @Max(9999)
        short year,
        
        @NotEmpty @Pattern(regexp= "french|english")
        String language,

        @Nullable
        Collection<AuthorDTO> authors
) {
}
