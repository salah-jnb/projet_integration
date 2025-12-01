using Microsoft.Extensions.Configuration;
using System.Net;
using System.Net.Mail;
using System.Threading.Tasks;

namespace JNBFitness.Application.Services.Communication
{
    public class EmailService : IEmailService
    {
        private readonly IConfiguration _configuration;

        public EmailService(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        public async Task SendAsync(string to, string subject, string htmlBody)
        {
            var host = _configuration["Email:SmtpHost"];
            var port = int.Parse(_configuration["Email:SmtpPort"] ?? "587");
            var sender = _configuration["Email:Sender"];
            var password = _configuration["Email:AppPassword"];

            using var client = new SmtpClient(host, port)
            {
                Credentials = new NetworkCredential(sender, password),
                EnableSsl = true
            };
            using var message = new MailMessage()
            {
                From = new MailAddress(sender, "JNB Fitness"),
                Subject = subject,
                Body = htmlBody,
                IsBodyHtml = true
            };
            message.To.Add(to);
            await client.SendMailAsync(message);
        }
    }
}