using JNBFitness.Application.DTOs.Produit;

namespace JNBFitness.Application.Services.Produit
{
    public interface IProduitService
    {
        Task<ProduitDto> CreateProduitAsync(CreateProduitDto createDto);
        Task<ProduitDto> UpdateProduitAsync(long id, UpdateProduitDto updateDto);
        Task<IEnumerable<ProduitDto>> GetProduitsActifsAsync();
        Task<IEnumerable<ProduitDto>> GetAllProduitsAsync();
        Task<bool> DeleteProduitAsync(long id);
    }
}
