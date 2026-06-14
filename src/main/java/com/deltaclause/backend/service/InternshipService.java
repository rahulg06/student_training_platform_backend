package com.deltaclause.backend.service;

import com.deltaclause.backend.entity.Internship;
import com.deltaclause.backend.entity.TaskSheetItem;
import com.deltaclause.backend.repository.InternshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;

    @Cacheable(value = "internships", key = "'all_listings'")
    public List<Internship> getAllInternships() {
        System.out.println("[Redis Cache Miss] Pulling all active trainings from MySQL database...");
        return internshipRepository.findAll();
    }

    @CacheEvict(value = "internships", allEntries = true)
    public Internship createInternship(Internship internship) {
        // Automatically set the ID if not provided
        if (internship.getId() == null || internship.getId().isEmpty()) {
            internship.setId("intern-" + internship.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        }
        
        // Link Task Sheets back to parent relation
        if (internship.getTaskSheets() != null) {
            for (TaskSheetItem item : internship.getTaskSheets()) {
                item.setInternship(internship);
            }
        }
        
        return internshipRepository.save(internship);
    }

    @CacheEvict(value = "internships", allEntries = true)
    public Optional<Internship> updateInternship(String id, Internship details) {
        return internshipRepository.findById(id).map(existing -> {
            existing.setName(details.getName());
            existing.setDetail(details.getDetail());
            existing.setPrice(details.getPrice());
            existing.setDuration(details.getDuration());
            existing.setDomains(details.getDomains());
            
            // Re-map tasks list
            existing.getTaskSheets().clear();
            if (details.getTaskSheets() != null) {
                for (TaskSheetItem item : details.getTaskSheets()) {
                    item.setInternship(existing);
                    existing.getTaskSheets().add(item);
                }
            }
            return internshipRepository.save(existing);
        });
    }

    @CacheEvict(value = "internships", allEntries = true)
    public boolean deleteInternship(String id) {
        if (internshipRepository.existsById(id)) {
            internshipRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
