namespace JNBFitness.Application.DTOs.Utilisateur
{
    public class ClientDto
    {
        public long UtilisateurId { get; set; }
        public string Email { get; set; }
        public string Nom { get; set; }
        public string Prenom { get; set; }
        public string Telephone { get; set; }
        public DateTime? DateActivation { get; set; }
        public string CodeParrainage { get; set; }
        public int NombreParrainagesValides { get; set; }
        public string ParrainePar { get; set; }
    }
}
