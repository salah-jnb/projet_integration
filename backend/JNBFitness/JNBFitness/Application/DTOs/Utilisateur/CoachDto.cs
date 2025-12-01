namespace JNBFitness.Application.DTOs.Utilisateur
{
    public class CoachDto
    {
        public long UtilisateurId { get; set; }
        public string Email { get; set; }
        public string Nom { get; set; }
        public string Prenom { get; set; }
        public string Photo { get; set; }
        public string Specialites { get; set; }
        public string Description { get; set; }
        public decimal NoteGlobale { get; set; }
        public int NombreAvis { get; set; }
    }
}
