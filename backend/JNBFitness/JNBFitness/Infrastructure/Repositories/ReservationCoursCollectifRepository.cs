using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;


namespace JNBFitness.Infrastructure.Repositories;

public class ReservationCoursCollectifRepository : Repository<ReservationCoursCollectif>, IReservationCoursCollectifRepository
{
    public ReservationCoursCollectifRepository(ApplicationDbContext context) : base(context)
    {
    }

    public async Task<IEnumerable<ReservationCoursCollectif>> GetReservationsByClientIdAsync(long clientId)
    {
        return await _dbSet
            .Include(r => r.SeanceCoursCollectif)
            .ThenInclude(s => s.CoursCollectif)
            .Where(r => r.ClientId == clientId)
            .OrderByDescending(r => r.DateReservation)
            .ToListAsync();
    }

    public async Task<IEnumerable<ReservationCoursCollectif>> GetReservationsBySeanceIdAsync(long seanceId)
    {
        return await _dbSet
            .Include(r => r.Client)
            .ThenInclude(c => c.Utilisateur)
            .Where(r => r.SeanceCoursCollectifId == seanceId)
            .ToListAsync();
    }
}
