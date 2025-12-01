using JNBFitness.Application.DTOs.Cours;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace JNBFitness.Application.Services.Cours
{
    public interface ISeanceCoursCollectifService
    {
        Task<IEnumerable<SeanceCoursCollectifDto>> GetSeancesDisponiblesAsync();
        Task<IEnumerable<SeanceCoursCollectifDto>> GetSeancesDisponiblesByCoachIdAsync(long coachId);
        Task<SeanceCoursCollectifDto> CreateSeanceAsync(CreateSeanceCoursCollectifDto createDto);
        Task<IEnumerable<SeanceCoursCollectifDto>> GetAllSeancesAsync();
        Task<SeanceCoursCollectifDto> UpdateSeanceAsync(long id, UpdateSeanceCoursCollectifDto updateDto);
        Task<bool> AnnulerSeanceAsync(long id);
        Task<bool> DeleteSeanceAsync(long id);
    }
}