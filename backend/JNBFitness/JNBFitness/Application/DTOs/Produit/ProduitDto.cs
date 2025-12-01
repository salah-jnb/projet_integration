namespace JNBFitness.Application.DTOs.Produit
{
    public class ProduitDto
    {
        public long Id { get; set; }
        public string Nom { get; set; }
        public string Description { get; set; }
        public decimal Prix { get; set; }
        public string Categorie { get; set; }
        public string ImageUrl { get; set; }
        public bool Actif { get; set; }
    }
}
