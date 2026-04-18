package ru.practicum.ewm.main.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.common.OffsetPageRequest;
import ru.practicum.ewm.main.dto.category.CategoryDto;
import ru.practicum.ewm.main.dto.category.NewCategoryDto;
import ru.practicum.ewm.main.error.ConflictException;
import ru.practicum.ewm.main.error.NotFoundException;
import ru.practicum.ewm.main.event.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto create(NewCategoryDto request) {
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.toEntity(request)));
    }

    @Transactional
    public CategoryDto update(Long catId, CategoryDto request) {
        Category category = getExisting(catId);
        category.setName(request.getName());
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public void delete(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(new OffsetPageRequest(from, size)).stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Long catId) {
        return CategoryMapper.toDto(getExisting(catId));
    }

    public Category getExisting(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
