using JNBFitness.Application.DTOs.Produit;
using JNBFitness.Application.Services.Produit;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using System.IO;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ProduitsController : ControllerBase
    {
        private readonly IProduitService _produitService;

        public ProduitsController(IProduitService produitService)
        {
            _produitService = produitService;
        }

        /// <summary>
        /// Récupérer tous les produits actifs
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<ProduitDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ProduitDto>>> GetAll()
        {
            var produits = await _produitService.GetProduitsActifsAsync();
            return Ok(produits);
        }

        /// <summary>
        /// Récupérer tous les produits (actifs et inactifs) - admin
        /// </summary>
        [HttpGet("all")]
        [Authorize]
        [ProducesResponseType(typeof(IEnumerable<ProduitDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ProduitDto>>> GetAllAdmin()
        {
            var produits = await _produitService.GetAllProduitsAsync();
            return Ok(produits);
        }

        /// <summary>
        /// Créer un nouveau produit
        /// </summary>
        [HttpPost]
        [Authorize]
        [RequestSizeLimit(20_000_000)]
        [Consumes("multipart/form-data")]
        [ProducesResponseType(typeof(ProduitDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ProduitDto>> Create([FromForm] CreateProduitDto createDto, IFormFile? image)
        {
            try
            {
                createDto.ImageUrl = null;
                if (Request.HasFormContentType)
                {
                    var nomRaw = Request.Form["Nom"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(nomRaw)) createDto.Nom = nomRaw;
                    var descRaw = Request.Form["Description"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(descRaw)) createDto.Description = descRaw;
                    var catRaw = Request.Form["Categorie"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(catRaw)) createDto.Categorie = catRaw;

                    var prixRaw = Request.Form["Prix"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(prixRaw))
                    {
                        if (decimal.TryParse(prixRaw, System.Globalization.NumberStyles.Number, System.Globalization.CultureInfo.InvariantCulture, out var prixInvariant))
                        {
                            createDto.Prix = prixInvariant;
                        }
                        else if (decimal.TryParse(prixRaw.Replace('.', ','), System.Globalization.NumberStyles.Number, new System.Globalization.CultureInfo("fr-FR"), out var prixFr))
                        {
                            createDto.Prix = prixFr;
                        }
                        else if (decimal.TryParse(prixRaw.Replace(',', '.'), System.Globalization.NumberStyles.Number, System.Globalization.CultureInfo.InvariantCulture, out var prixInvariant2))
                        {
                            createDto.Prix = prixInvariant2;
                        }
                        else
                        {
                            return BadRequest(new { errors = new { Prix = new[] { "Prix invalide (utilisez un décimal)" } } });
                        }
                    }
                    ModelState.Clear();
                }

                if (string.IsNullOrWhiteSpace(createDto.Nom))
                    return BadRequest(new { errors = new { Nom = new[] { "Nom requis" } } });
                if (createDto.Prix <= 0)
                    return BadRequest(new { errors = new { Prix = new[] { "Le prix doit être positif" } } });

                if (image != null && image.Length > 0)
                {
                    var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", "products");
                    if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

                    var ext = Path.GetExtension(image.FileName);
                    var safeName = (createDto.Nom ?? "produit").Trim().Replace(" ", "_").Replace(".", "_");
                    var fileName = $"{safeName}_{DateTime.Now:yyyyMMddHHmmss}{ext}";
                    var filePath = Path.Combine(uploadsDir, fileName);
                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await image.CopyToAsync(stream);
                    }
                    createDto.ImageUrl = $"/uploads/products/{fileName}";
                }

                var produit = await _produitService.CreateProduitAsync(createDto);
                return CreatedAtAction(nameof(GetAll), produit);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour un produit
        /// </summary>
        [HttpPut("{id}")]
        [Authorize]
        [RequestSizeLimit(20_000_000)]
        [Consumes("multipart/form-data")]
        [ProducesResponseType(typeof(ProduitDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ProduitDto>> Update(long id, [FromForm] UpdateProduitDto updateDto, IFormFile? image)
        {
            try
            {
                updateDto.ImageUrl = null;
                if (Request.HasFormContentType)
                {
                    var nomRaw = Request.Form["Nom"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(nomRaw)) updateDto.Nom = nomRaw;
                    var descRaw = Request.Form["Description"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(descRaw)) updateDto.Description = descRaw;
                    var catRaw = Request.Form["Categorie"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(catRaw)) updateDto.Categorie = catRaw;
                    var actifRaw = Request.Form["Actif"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(actifRaw) && bool.TryParse(actifRaw, out var actifVal)) updateDto.Actif = actifVal;

                    var prixRaw = Request.Form["Prix"].FirstOrDefault();
                    if (!string.IsNullOrWhiteSpace(prixRaw))
                    {
                        if (decimal.TryParse(prixRaw, System.Globalization.NumberStyles.Number, System.Globalization.CultureInfo.InvariantCulture, out var prixInvariant))
                        {
                            updateDto.Prix = prixInvariant;
                        }
                        else if (decimal.TryParse(prixRaw.Replace('.', ','), System.Globalization.NumberStyles.Number, new System.Globalization.CultureInfo("fr-FR"), out var prixFr))
                        {
                            updateDto.Prix = prixFr;
                        }
                        else if (decimal.TryParse(prixRaw.Replace(',', '.'), System.Globalization.NumberStyles.Number, System.Globalization.CultureInfo.InvariantCulture, out var prixInvariant2))
                        {
                            updateDto.Prix = prixInvariant2;
                        }
                        else
                        {
                            return BadRequest(new { errors = new { Prix = new[] { "Prix invalide (utilisez un décimal)" } } });
                        }
                    }
                    ModelState.Clear();
                }

                if (image != null && image.Length > 0)
                {
                    var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", "products");
                    if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

                    var ext = Path.GetExtension(image.FileName);
                    var fileName = $"product_{id}_{DateTime.Now:yyyyMMddHHmmss}{ext}";
                    var filePath = Path.Combine(uploadsDir, fileName);
                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await image.CopyToAsync(stream);
                    }
                    updateDto.ImageUrl = $"/uploads/products/{fileName}";
                }

                var produit = await _produitService.UpdateProduitAsync(id, updateDto);
                return Ok(produit);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Supprimer un produit
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Delete(long id)
        {
            try
            {
                await _produitService.DeleteProduitAsync(id);
                return Ok(new { message = "Produit supprimé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
