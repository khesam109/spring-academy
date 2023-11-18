package com.khesam.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

/**
 * {@code @RestController} tells Spring that this class is a Component of type RestController
 * and capable of handling HTTP requests.
 * <p>
 * {@code @RequestMapping} is a companion to @RestController that
 * indicates which address requests must have to access this Controller.
 */
@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    @Autowired
    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    /**
     * {@code @GetMapping} marks a method as a handler method.
     */
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     *
     * @param pageable is the object that Spring Web provides for us.
     *                Since we specified the URI parameters of page=0&size=1,
     *                pageable will contain the values we need.
     */
    @GetMapping
    public ResponseEntity<Iterable<CashCard>> findAll(
            @PageableDefault(size = 3, page = 0) Pageable pageable, Principal principal
    ) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    /**
     * @param ucb Injected from Spring's IoC Container
     */
    @PostMapping
    public ResponseEntity<Void> createCashCard(
            @RequestBody CashCard newCashCardRequest,
            UriComponentsBuilder ucb,
            Principal principal
    ) {
        //Supplying an id to cashCardRepository.save is supported when an update is performed on an existing resource.
        CashCard savedCashCard = cashCardRepository.save(new CashCard(
                null, newCashCardRequest.amount(), principal.getName()
        ));
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(
                locationOfNewCashCard
        ).build();
    }

    @PutMapping("/{requestedId}")
    public ResponseEntity<Void> putCashCard(
            @PathVariable Long requestedId,
            @RequestBody CashCard cashCardUpdate,
            Principal principal
    ) {
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashCard(
            @PathVariable Long id,
            Principal principal
    ) {
        if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }
}
