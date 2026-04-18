package ru.practicum.ewm.main.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.common.OffsetPageRequest;
import ru.practicum.ewm.main.dto.user.NewUserRequest;
import ru.practicum.ewm.main.dto.user.UserDto;
import ru.practicum.ewm.main.error.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDto create(NewUserRequest request) {
        return UserMapper.toDto(userRepository.save(UserMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<UserDto> get(List<Long> ids, int from, int size) {
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(new OffsetPageRequest(from, size)).stream()
                    .map(UserMapper::toDto)
                    .toList();
        }
        return userRepository.findByIdIn(ids, PageRequest.of(from / size, size)).stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }

    public User getExisting(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }
}
