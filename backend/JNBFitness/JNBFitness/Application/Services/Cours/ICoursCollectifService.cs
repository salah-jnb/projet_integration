using JNBFitness.Application.DTOs.Cours;

namespace JNBFitness.Application.Services.Cours
{
    public interface ICoursCollectifService
    {
        Task<CoursCollectifDto> CreateCoursAsync(CreateCoursCollectifDto createDto);
        Task<CoursCollectifDto> UpdateCoursAsync(long id, UpdateCoursCollectifDto updateDto);
        Task<IEnumerable<CoursCollectifDto>> GetCoursActifsAsync();
        Task<CoursCollectifDto> GetCoursByIdAsync(long id);
        Task<bool> DeleteCoursAsync(long id);
    }
}
