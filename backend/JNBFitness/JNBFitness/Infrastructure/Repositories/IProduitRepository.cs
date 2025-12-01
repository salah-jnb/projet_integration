using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IProduitRepository : IRepository<Produit>
    {
        Task<IEnumerable<Produit>> GetProduitsActifsAsync();
        Task<IEnumerable<Produit>> GetProduitsByCategorieAsync(string categorie);
    }
}
