package com.qdeb.service;

import com.qdeb.dto.ApplicationFieldDto;
import com.qdeb.dto.TournamentApplicationRequest;
import com.qdeb.dto.TournamentApplicationResponse;
import com.qdeb.dto.TournamentApplicationDetailResponse;
import com.qdeb.entity.*;
import com.qdeb.repository.TournamentApplicationRepository;
import com.qdeb.repository.TournamentRepository;
import com.qdeb.repository.TeamRepository;
import com.qdeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentApplicationService {
    
    private final TournamentApplicationRepository applicationRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TabbycatIntegrationService tabbycatIntegrationService;
    
    @Transactional
    public TournamentApplicationResponse submitApplication(Long tournamentId, TournamentApplicationRequest request, String username) {
        log.info("Подача заявки на турнир {} от пользователя {}", tournamentId, username);
        
        // 1. Получаем пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        
        // 2. Проверяем турнир
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Турнир не найден"));
        
        // 3. Проверяем активность турнира
        if (!tournament.isActive()) {
            log.warn("Попытка подачи заявки на неактивный турнир {} от пользователя {}", tournamentId, username);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament is not active");
        }
        
        // 4. Получаем команду
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Команда не найдена"));
        
        // 5. Проверяем, что пользователь является участником команды
        if (!isUserInTeam(user, team)) {
            log.warn("Пользователь {} не является участником команды {}", username, request.getTeamId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не является участником команды");
        }
        
        // 6. Проверяем, что пользователь является капитаном команды
        if (!team.getLeader().getId().equals(user.getId())) {
            log.warn("Пользователь {} не является капитаном команды {}", username, request.getTeamId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только капитан команды может подавать заявки");
        }
        
        // 7. Проверяем количество участников команды
        int memberCount = team.getMemberCount();
        if (memberCount != 2) {
            log.warn("Команда {} имеет {} участников, требуется 2", request.getTeamId(), memberCount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команда должна иметь ровно 2 участника");
        }
        
        // 8. Проверяем, что у пользователя нет активных заявок на этот конкретный турнир (PENDING или APPROVED)
        List<ApplicationStatus> activeStatuses = List.of(ApplicationStatus.PENDING, ApplicationStatus.APPROVED);
        List<TournamentApplication> activeApplicationsOnThisTournament = applicationRepository
                .findBySubmittedByIdAndStatusIn(user.getId(), activeStatuses)
                .stream()
                .filter(app -> app.getTournament().getId().equals(tournamentId))
                .toList();
        
        if (!activeApplicationsOnThisTournament.isEmpty()) {
            log.warn("Пользователь {} уже имеет активную заявку на турнир {}", username, tournamentId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "У вас уже есть активная заявка на этот турнир. Вы не можете подавать повторные заявки, пока не будет рассмотрена текущая.");
        }
        
        // 8.1. Дополнительная проверка: если у пользователя есть только REJECTED заявки на этот турнир, разрешаем подавать новую
        List<TournamentApplication> allApplicationsOnThisTournament = applicationRepository
                .findBySubmittedById(user.getId())
                .stream()
                .filter(app -> app.getTournament().getId().equals(tournamentId))
                .toList();
        
        boolean hasOnlyRejectedApplications = !allApplicationsOnThisTournament.isEmpty() && 
                allApplicationsOnThisTournament.stream()
                        .allMatch(app -> app.getStatus() == ApplicationStatus.REJECTED);
        
        if (hasOnlyRejectedApplications) {
            log.info("У пользователя {} есть только отклоненные заявки на турнир {}, разрешаем подать новую", username, tournamentId);
        }
        
        // 9. Проверяем, что команда еще не подавала активную заявку на этот турнир
        Optional<TournamentApplication> existingTeamApplication = applicationRepository.findByTournamentIdAndTeamId(tournamentId, request.getTeamId());
        if (existingTeamApplication.isPresent()) {
            TournamentApplication existingApp = existingTeamApplication.get();
            if (existingApp.getStatus() == ApplicationStatus.PENDING || existingApp.getStatus() == ApplicationStatus.APPROVED) {
                log.warn("Команда {} уже имеет активную заявку на турнир {}", request.getTeamId(), tournamentId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команда уже имеет активную заявку на этот турнир");
            } else if (existingApp.getStatus() == ApplicationStatus.REJECTED) {
                log.info("У команды {} есть отклоненная заявка на турнир {}, разрешаем подать новую", request.getTeamId(), tournamentId);
            }
        }
        
        // 10. Валидируем поля регистрации
        validateRegistrationFields(tournament, request.getFields());
        
        // 11. Создаем заявку
        TournamentApplication application = new TournamentApplication();
        application.setTournament(tournament);
        application.setTeam(team);
        application.setSubmittedBy(user);
        application.setStatus(ApplicationStatus.PENDING);
        
        // 12. Сохраняем заявку
        TournamentApplication savedApplication = applicationRepository.save(application);
        log.info("Заявка сохранена с ID: {}", savedApplication.getId());
        
        // 13. Создаем поля заявки
        List<TournamentApplicationField> applicationFields = new ArrayList<>();
        for (ApplicationFieldDto fieldDto : request.getFields()) {
            TournamentApplicationField field = new TournamentApplicationField();
            field.setName(fieldDto.getName());
            field.setValue(fieldDto.getValue());
            field.setApplication(savedApplication);
            applicationFields.add(field);
        }
        
        // Сохраняем поля (cascade автоматически сохранит их)
        savedApplication.setFields(applicationFields);
        
        log.info("Заявка на турнир {} успешно подана от команды {}", tournamentId, request.getTeamId());
        
        return new TournamentApplicationResponse("Application submitted successfully", savedApplication.getId());
    }
    
    private boolean isUserInTeam(User user, Team team) {
        // Проверяем, является ли пользователь лидером команды
        if (team.getLeader().getId().equals(user.getId())) {
            return true;
        }
        
        // Проверяем, является ли пользователь участником команды
        return team.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));
    }
    
    private void validateRegistrationFields(Tournament tournament, List<ApplicationFieldDto> submittedFields) {
        List<RegistrationField> requiredFields = tournament.getRegistrationFields().stream()
                .filter(RegistrationField::isRequired)
                .collect(Collectors.toList());
        
        // Создаем Map для быстрого поиска по имени поля
        Map<String, String> submittedFieldsMap = submittedFields.stream()
                .collect(Collectors.toMap(
                        ApplicationFieldDto::getName,
                        ApplicationFieldDto::getValue,
                        (existing, replacement) -> replacement // В случае дубликатов берем последний
                ));
        
        // Проверяем все обязательные поля
        for (RegistrationField requiredField : requiredFields) {
            String fieldName = requiredField.getName();
            if (!submittedFieldsMap.containsKey(fieldName) || 
                submittedFieldsMap.get(fieldName) == null || 
                submittedFieldsMap.get(fieldName).trim().isEmpty()) {
                
                log.warn("Отсутствует обязательное поле: {}", fieldName);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required field: " + fieldName);
            }
        }
        
        log.info("Все обязательные поля валидны");
    }
    
    public List<TournamentApplicationDetailResponse> getApplicationsByTournamentSlug(String tournamentSlug) {
        return getApplicationsByTournamentSlug(tournamentSlug, null);
    }
    
    public List<TournamentApplicationDetailResponse> getApplicationsByTournamentSlug(String tournamentSlug, ApplicationStatus status) {
        log.info("Получение заявок для турнира с slug: {}, статус: {}", tournamentSlug, status);
        
        // Находим турнир по slug
        Tournament tournament = tournamentRepository.findBySlug(tournamentSlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Турнир не найден"));
        
        // Получаем заявки на этот турнир с фильтрацией по статусу
        List<TournamentApplication> applications;
        if (status != null) {
            applications = applicationRepository.findByTournamentIdAndStatus(tournament.getId(), status);
            log.info("Найдено {} заявок для турнира {} со статусом {}", applications.size(), tournamentSlug, status);
        } else {
            applications = applicationRepository.findByTournamentId(tournament.getId());
            log.info("Найдено {} заявок для турнира {}", applications.size(), tournamentSlug);
        }
        
        // Преобразуем в DTO
        return applications.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }
    
    private TournamentApplicationDetailResponse convertToDetailResponse(TournamentApplication application) {
        TournamentApplicationDetailResponse response = new TournamentApplicationDetailResponse();
        
        // Основная информация о заявке
        response.setId(application.getId());
        response.setTournamentId(application.getTournament().getId());
        response.setTournamentName(application.getTournament().getName());
        response.setTournamentSlug(application.getTournament().getSlug());
        response.setStatus(application.getStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        
        // Информация о подавшем заявку
        User submittedBy = application.getSubmittedBy();
        TournamentApplicationDetailResponse.UserInfo submittedByInfo = new TournamentApplicationDetailResponse.UserInfo(
                submittedBy.getId(),
                submittedBy.getUsername(),
                submittedBy.getEmail(),
                submittedBy.getFullName(),
                submittedBy.getPhone(),
                submittedBy.getDescription(),
                submittedBy.getProfilePicture(),
                submittedBy.getCreatedAt()
        );
        response.setSubmittedBy(submittedByInfo);
        
        // Информация о команде
        Team team = application.getTeam();
        User leader = team.getLeader();
        User member = team.getMembers().stream()
                .filter(tm -> !tm.getUser().getId().equals(leader.getId()))
                .findFirst()
                .map(TeamMember::getUser)
                .orElse(null);
        
        TournamentApplicationDetailResponse.UserInfo leaderInfo = new TournamentApplicationDetailResponse.UserInfo(
                leader.getId(),
                leader.getUsername(),
                leader.getEmail(),
                leader.getFullName(),
                leader.getPhone(),
                leader.getDescription(),
                leader.getProfilePicture(),
                leader.getCreatedAt()
        );
        
        TournamentApplicationDetailResponse.UserInfo memberInfo = null;
        if (member != null) {
            memberInfo = new TournamentApplicationDetailResponse.UserInfo(
                    member.getId(),
                    member.getUsername(),
                    member.getEmail(),
                    member.getFullName(),
                    member.getPhone(),
                    member.getDescription(),
                    member.getProfilePicture(),
                    member.getCreatedAt()
            );
        }
        
        TournamentApplicationDetailResponse.TeamInfo teamInfo = new TournamentApplicationDetailResponse.TeamInfo(
                team.getId(),
                team.getName(),
                team.getJoinCode(),
                leaderInfo,
                memberInfo,
                team.getMemberCount(),
                team.isFull(),
                team.getCreatedAt()
        );
        response.setTeam(teamInfo);
        
        // Поля заявки
        List<TournamentApplicationDetailResponse.ApplicationFieldResponse> fieldResponses = application.getFields().stream()
                .map(field -> new TournamentApplicationDetailResponse.ApplicationFieldResponse(
                        field.getId(),
                        field.getName(),
                        field.getValue()
                ))
                .collect(Collectors.toList());
        response.setFields(fieldResponses);
        
        return response;
    }
    
    public List<TournamentApplicationDetailResponse> getApplicationsByTeamId(Long teamId) {
        return getApplicationsByTeamId(teamId, null);
    }
    
    public List<TournamentApplicationDetailResponse> getApplicationsByTeamId(Long teamId, ApplicationStatus status) {
        log.info("Получение заявок для команды с ID: {}, статус: {}", teamId, status);
        
        // Проверяем, что команда существует
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Команда не найдена"));
        
        // Получаем заявки этой команды с фильтрацией по статусу
        List<TournamentApplication> applications;
        if (status != null) {
            applications = applicationRepository.findByTeamIdAndStatus(teamId, status);
            log.info("Найдено {} заявок для команды {} со статусом {}", applications.size(), teamId, status);
        } else {
            applications = applicationRepository.findByTeamId(teamId);
            log.info("Найдено {} заявок для команды {}", applications.size(), teamId);
        }
        
        // Преобразуем в DTO
        return applications.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TournamentApplicationDetailResponse acceptApplication(Long applicationId) {
        log.info("Принятие заявки с ID: {}", applicationId);
        
        TournamentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));
        
        // Проверяем, что заявка в статусе PENDING
        if (application.getStatus() != ApplicationStatus.PENDING) {
            log.warn("Попытка принять заявку {} в статусе {}", applicationId, application.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Можно принимать только заявки в статусе PENDING. Текущий статус: " + application.getStatus());
        }
        
        // Обновляем статус
        application.setStatus(ApplicationStatus.APPROVED);
        application.setUpdatedAt(java.time.LocalDateTime.now());
        
        TournamentApplication savedApplication = applicationRepository.save(application);
        
        log.info("Заявка {} успешно принята", applicationId);
        
        // Создаем команду в Tabbycat
        try {
            log.info("Создание команды в Tabbycat для принятой заявки {}", applicationId);
            tabbycatIntegrationService.createTeamInTabbycat(savedApplication);
            log.info("Команда успешно создана в Tabbycat для заявки {}", applicationId);
        } catch (Exception e) {
            log.error("Ошибка при создании команды в Tabbycat для заявки {}: {}", 
                    applicationId, e.getMessage(), e);
            // Не прерываем процесс принятия заявки, только логируем ошибку
        }
        
        return convertToDetailResponse(savedApplication);
    }
    
    @Transactional
    public TournamentApplicationDetailResponse rejectApplication(Long applicationId) {
        log.info("Отклонение заявки с ID: {}", applicationId);
        
        TournamentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена"));
        
        // Проверяем, что заявка в статусе PENDING
        if (application.getStatus() != ApplicationStatus.PENDING) {
            log.warn("Попытка отклонить заявку {} в статусе {}", applicationId, application.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Можно отклонять только заявки в статусе PENDING. Текущий статус: " + application.getStatus());
        }
        
        // Обновляем статус
        application.setStatus(ApplicationStatus.REJECTED);
        application.setUpdatedAt(java.time.LocalDateTime.now());
        
        TournamentApplication savedApplication = applicationRepository.save(application);
        
        log.info("Заявка {} успешно отклонена", applicationId);
        
        return convertToDetailResponse(savedApplication);
    }
}
