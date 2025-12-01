using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace JNBFitness.Infrastructure.Repositories
{
    public class SeanceCoursCollectifRepository : Repository<SeanceCoursCollectif>, ISeanceCoursCollectifRepository
    {
        public SeanceCoursCollectifRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<SeanceCoursCollectif>> GetSeancesDisponiblesAsync()
        {
            var now = DateTime.Now;
            return await _context.SeancesCoursCollectifs
                .Include(s => s.CoursCollectif)
                .Where(s => !s.Annulee && s.PlacesDisponibles > 0 && s.DateSeance >= now && s.CoursCollectif.Actif)
                .OrderBy(s => s.DateSeance)
                .ToListAsync();
        }

        public async Task<IEnumerable<SeanceCoursCollectif>> GetSeancesDisponiblesByCoachIdAsync(long coachId)
        {
            var now = DateTime.Now;
            return await _context.SeancesCoursCollectifs
                .Include(s => s.CoursCollectif)
                .Where(s => !s.Annulee
                            && s.PlacesDisponibles > 0
                            && s.DateSeance >= now
                            && s.CoursCollectif.Actif
                            && s.CoursCollectif.CoachId == coachId)
                .OrderBy(s => s.DateSeance)
                .ToListAsync();
        }

        public async Task<IEnumerable<SeanceCoursCollectif>> GetAllWithCoursAsync()
        {
            return await _context.SeancesCoursCollectifs
                .Include(s => s.CoursCollectif)
                .OrderByDescending(s => s.DateSeance)
                .ToListAsync();
        }
    }
}