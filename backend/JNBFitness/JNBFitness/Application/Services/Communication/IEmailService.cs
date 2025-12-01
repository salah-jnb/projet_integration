using System.Threading.Tasks;

namespace JNBFitness.Application.Services.Communication
{
    public interface IEmailService
    {
        Task SendAsync(string to, string subject, string htmlBody);
    }
}