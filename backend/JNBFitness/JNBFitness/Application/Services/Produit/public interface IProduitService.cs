using AutoMapper;
using JNBFitness.Application.DTOs.Produit;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Produit
{
    public class ProduitService : IProduitService
    {
        private readonly IProduitRepository _produitRepository;
        private readonly IMapper _mapper;

        public ProduitService(IProduitRepository produitRepository, IMapper mapper)
        {
            _produitRepository = produitRepository;
            _mapper = mapper;
        }

        public async Task<ProduitDto> CreateProduitAsync(CreateProduitDto createDto)
        {
            var produit = _mapper.Map<Domain.Entities.Produit>(createDto);
            produit.Actif = true;

            produit = await _produitRepository.AddAsync(produit);
            return _mapper.Map<ProduitDto>(produit);
        }

        public async Task<ProduitDto> UpdateProduitAsync(long id, UpdateProduitDto updateDto)
        {
            var produit = await _produitRepository.GetByIdAsync(id);
            if (produit == null)
                throw new Exception("Produit introuvable");

            if (!string.IsNullOrEmpty(updateDto.Nom))
                produit.Nom = updateDto.Nom;
            if (!string.IsNullOrEmpty(updateDto.Description))
                produit.Description = updateDto.Description;
            if (updateDto.Prix.HasValue)
                produit.Prix = updateDto.Prix.Value;
            if (!string.IsNullOrEmpty(updateDto.Categorie))
                produit.Categorie = updateDto.Categorie;
            if (!string.IsNullOrEmpty(updateDto.ImageUrl))
                produit.ImageUrl = updateDto.ImageUrl;
            if (updateDto.Actif.HasValue)
                produit.Actif = updateDto.Actif.Value;

            await _produitRepository.UpdateAsync(produit);
            return _mapper.Map<ProduitDto>(produit);
        }

        public async Task<IEnumerable<ProduitDto>> GetProduitsActifsAsync()
        {
            var produits = await _produitRepository.GetProduitsActifsAsync();
            return _mapper.Map<IEnumerable<ProduitDto>>(produits);
        }

        public async Task<IEnumerable<ProduitDto>> GetAllProduitsAsync()
        {
            var produits = await _produitRepository.GetAllAsync();
            return _mapper.Map<IEnumerable<ProduitDto>>(produits);
        }

        public async Task<bool> DeleteProduitAsync(long id)
        {
            var produit = await _produitRepository.GetByIdAsync(id);
            if (produit == null)
                throw new Exception("Produit introuvable");

            await _produitRepository.DeleteAsync(produit);
            return true;
        }
    }
}
