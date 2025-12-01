using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Configuration;
using System.Threading;
using System.Threading.Tasks;
using MySqlConnector;
using System.Collections.Generic;
using JNBFitness.Application.Services.Communication;
using JNBFitness.Application.DTOs.Notification;
using Microsoft.Extensions.DependencyInjection;

namespace JNBFitness.Application.Services.Notification
{
    public class NotificationQueueHostedService : BackgroundService
    {
        private readonly IConfiguration _configuration;
        private readonly IServiceScopeFactory _scopeFactory;

        public NotificationQueueHostedService(IConfiguration configuration, IServiceScopeFactory scopeFactory)
        {
            _configuration = configuration;
            _scopeFactory = scopeFactory;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            while (!stoppingToken.IsCancellationRequested)
            {
                await ProcessAsync(stoppingToken);
                await Task.Delay(System.TimeSpan.FromSeconds(10), stoppingToken);
            }
        }

        private async Task ProcessAsync(CancellationToken ct)
        {
            var connectionString = _configuration.GetConnectionString("DefaultConnection");
            using var conn = new MySqlConnection(connectionString);
            await conn.OpenAsync(ct);

            using var cmd = new MySqlCommand("SELECT id, client_id, abonnement_id FROM notifications_queue WHERE traite = FALSE", conn);
            using var reader = await cmd.ExecuteReaderAsync(ct);
            var items = new List<(long id, long clientId, long abonnementId)>();
            while (await reader.ReadAsync(ct))
            {
                var id = reader.GetInt64(0);
                var clientId = reader.GetInt64(1);
                var abonnementId = reader.GetInt64(2);
                items.Add((id, clientId, abonnementId));
            }
            await reader.CloseAsync();

            foreach (var item in items)
            {
                string email = string.Empty;
                using (var emailCmd = new MySqlCommand("SELECT email FROM utilisateurs WHERE id = @id", conn))
                {
                    emailCmd.Parameters.AddWithValue("@id", item.clientId);
                    var obj = await emailCmd.ExecuteScalarAsync(ct);
                    email = obj as string ?? string.Empty;
                }

                using (var scope = _scopeFactory.CreateScope())
                {
                    var emailService = scope.ServiceProvider.GetRequiredService<IEmailService>();
                    if (!string.IsNullOrWhiteSpace(email))
                    {
                        Console.WriteLine($"[HostedService] Envoi email expiré à: {email} | ClientId: {item.clientId} | AbonnementId: {item.abonnementId}");
                        var htmlBody =
                            "<div style='font-family:Arial,Helvetica,sans-serif'><h2 style='color:#0ea5e9;margin:0'>Abonnement expiré</h2><p>Votre abonnement a expiré.</p></div>";
                        await emailService.SendAsync(email, "Abonnement expiré", htmlBody);
                    }
                }

                using (var scope = _scopeFactory.CreateScope())
                {
                    var notificationService = scope.ServiceProvider.GetRequiredService<INotificationService>();
                    await notificationService.CreateNotificationAsync(new CreateNotificationDto
                    {
                        DestinataireId = item.clientId,
                        Titre = "Abonnement expiré",
                        Message = "Votre abonnement a expiré.",
                        Type = "ABONNEMENT_EXPIRE"
                    });
                }

                using var updateCmd = new MySqlCommand("UPDATE notifications_queue SET traite = TRUE WHERE id = @id", conn);
                updateCmd.Parameters.AddWithValue("@id", item.id);
                await updateCmd.ExecuteNonQueryAsync(ct);
            }
        }

        
    }
}