namespace JNBFitness.Application.DTOs.Auth
{
    public class LoginResponseDto
    {
        public string Token { get; set; }
        public DateTime Expiration { get; set; }
        public long UtilisateurId { get; set; }
        public string Email { get; set; }
        public string Nom { get; set; }
        public string Prenom { get; set; }
        public string TypeUtilisateur { get; set; }
    }
}
