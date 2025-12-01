using AutoMapper;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Communication;

namespace JNBFitness.Application.Services.Notification
{
    public class NotificationService : INotificationService
    {
        private readonly INotificationRepository _notificationRepository;
        private readonly IMapper _mapper;
        private readonly IEmailService _emailService;
        private readonly IUtilisateurRepository _utilisateurRepository;

        public NotificationService(INotificationRepository notificationRepository, IMapper mapper, IEmailService emailService, IUtilisateurRepository utilisateurRepository)
        {
            _notificationRepository = notificationRepository;
            _mapper = mapper;
            _emailService = emailService;
            _utilisateurRepository = utilisateurRepository;
        }

        public async Task<NotificationDto> CreateNotificationAsync(CreateNotificationDto createDto)
        {
            var notification = new Domain.Entities.Notification
            {
                DestinataireId = createDto.DestinataireId,
                Titre = createDto.Titre,
                Message = createDto.Message,
                Type = createDto.Type,
                DateEnvoi = DateTime.Now,
                Lue = false,
                EmailEnvoye = false
            };

            notification = await _notificationRepository.AddAsync(notification);

            var destinataire = await _utilisateurRepository.GetByIdAsync(notification.DestinataireId);
            if (destinataire != null && !string.IsNullOrWhiteSpace(destinataire.Email))
            {
                Console.WriteLine($"[NotificationService] Envoi email à: {destinataire.Email} | Type: {createDto.Type} | Titre: {createDto.Titre}");
                var subject = BuildEmailSubject(createDto.Type, createDto.Titre);
                var htmlBody = BuildEmailHtml(createDto.Type, createDto.Titre, createDto.Message, destinataire.Prenom ?? destinataire.Nom ?? "");
                await _emailService.SendAsync(destinataire.Email, subject, htmlBody);
                notification.EmailEnvoye = true;
                await _notificationRepository.UpdateAsync(notification);
            }

            return _mapper.Map<NotificationDto>(notification);
        }

        public async Task<IEnumerable<NotificationDto>> GetNotificationsByUtilisateurIdAsync(long utilisateurId)
        {
            var notifications = await _notificationRepository.GetNotificationsByUtilisateurIdAsync(utilisateurId);
            return _mapper.Map<IEnumerable<NotificationDto>>(notifications);
        }

        public async Task<IEnumerable<NotificationDto>> GetNotificationsNonLuesAsync(long utilisateurId)
        {
            var notifications = await _notificationRepository.GetNotificationsNonLuesAsync(utilisateurId);
            return _mapper.Map<IEnumerable<NotificationDto>>(notifications);
        }

        public async Task<bool> MarquerCommeLueAsync(long notificationId)
        {
            var notification = await _notificationRepository.GetByIdAsync(notificationId);
            if (notification == null)
                throw new Exception("Notification introuvable");

            notification.Lue = true;
            await _notificationRepository.UpdateAsync(notification);

            return true;
        }

        private string BuildEmailSubject(string type, string titre)
        {
            return titre;
        }

        private string BuildEmailHtml(string type, string titre, string message, string destinatairePrenom)
        {
            var accent = "#0ea5e9";
            var icon = type switch
            {
                "TRANSFERT_RECU" => "💸",
                "RECHARGE_CARTE" => "💳",
                "ARTICLE_PUBLIE" => "📝",
                "ARTICLE_REJETE" => "⚠️",
                "AVIS_RECU" => "⭐",
                "RESERVATION_COACHING" => "📅",
                "RESERVATION_CONFIRMEE" => "✅",
                "RESERVATION_ANNULEE" => "❌",
                "RAPPEL_NOTATION" => "🔔",
                "RESERVATION_COURS_CONFIRMEE" => "✅",
                "RESERVATION_COURS" => "📣",
                "RESERVATION_COURS_ANNULEE" => "❌",
                "SEANCE_PLANIFIEE" => "📆",
                "PARRAINAGE_VALIDE" => "🎁",
                "ABONNEMENT_EXPIRE" => "⏳",
                _ => "🏋️"
            };

            return $@"<div style='font-family:Arial,Helvetica,sans-serif;background:#f6f9fc;padding:24px;'>
  <div style='max-width:640px;margin:0 auto;background:#ffffff;border-radius:12px;box-shadow:0 6px 20px rgba(0,0,0,0.06);overflow:hidden;'>
    <div style='background:{accent};color:#fff;padding:18px 24px;font-size:18px;'>
      <span style='font-size:22px;margin-right:8px'>{icon}</span> {titre}
    </div>
    <div style='padding:24px;color:#111827;'>
      <p style='margin:0 0 8px 0;'>Bonjour {destinatairePrenom},</p>
      <p style='margin:0 0 16px 0;line-height:1.6'>{message}</p>
      <div style='margin:24px 0;padding:12px 16px;background:#f1f5f9;border-radius:8px;color:#334155;font-size:14px;'>
        JNB Fitness • Des performances au quotidien
      </div>
      <a href='https://jnbfitness.com' style='display:inline-block;margin-top:8px;padding:10px 16px;background:{accent};color:#fff;border-radius:8px;text-decoration:none;font-weight:600'>Ouvrir l'application</a>
    </div>
    <div style='padding:14px 24px;background:#f8fafc;color:#64748b;font-size:12px'>
      Vous recevez cet e-mail suite à une notification de votre compte JNB Fitness.
    </div>
  </div>
</div>";
        }
    }

}
