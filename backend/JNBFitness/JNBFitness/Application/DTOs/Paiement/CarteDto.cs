namespace JNBFitness.Application.DTOs.Paiement
{
    public class CarteDto
    {
        public long Id { get; set; }
        public long UtilisateurId { get; set; }
        public string Numero { get; set; }
        public string Libelle { get; set; }
        public string Devise { get; set; }
        public long SoldeCent { get; set; }
        public decimal SoldeEuro => SoldeCent / 100.0m;
        public bool Active { get; set; }
        public DateTime DateCreation { get; set; }
        public DateTime DateMiseAJour { get; set; }
    }
}
