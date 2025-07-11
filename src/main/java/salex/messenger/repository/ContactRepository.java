package salex.messenger.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import salex.messenger.entity.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    @Query("SELECT c FROM Contact c WHERE c.owner.id = :id")
    List<Contact> getAllContactsByOwnerId(@Param("id") Long ownerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Contact c WHERE c.owner.id = :ownerId AND c.contact.id = :contactId")
    void deleteContactByOwnerAndContactIds(@Param("ownerId") Long ownerId, @Param("contactId") Long contactId);

    @Query("SELECT c FROM Contact c WHERE c.owner.id = :ownerId AND c.contact.id = :contactId")
    Contact existsContact(@Param("ownerId") Long ownerId, @Param("contactId") Long contactId);
}
